/**
 * Pipeline Dashboard — WebSocket client, phase tree, log, file browser.
 * No AutoGen agents — phases run as subprocesses only.
 */
(function () {
    "use strict";

    // --- DOM refs ---
    const logMessages = document.getElementById("log-messages");
    const connStatus = document.getElementById("connection-status");
    const fileList = document.getElementById("file-list");
    const filePreview = document.getElementById("file-preview");
    const previewHeader = document.getElementById("preview-header");
    const previewCode = document.getElementById("preview-code");
    const pipelineTree = document.getElementById("pipeline-tree");
    const publishBtn = document.getElementById("publish-btn");
    const clearBtn = document.getElementById("clear-btn");
    const resetAllBtn = document.getElementById("reset-all-btn");
    const cleanProjectBtn = document.getElementById("clean-project-btn");
    const workingIndicator = document.getElementById("working-indicator");
    const workingText = document.getElementById("working-text");
    const feedbackBody = document.getElementById("feedback-body");
    const feedbackTitle = document.getElementById("feedback-title");
    const feedbackSubtitle = document.getElementById("feedback-subtitle");
    const feedbackCopyBtn = document.getElementById("feedback-copy-btn");

    // --- State ---
    let ws = null;
    let autoScroll = true;
    let taskStatuses = {};
    let selectedTask = null;
    let workingTimerId = null;
    let workingStartTime = null;
    let liveStreamEl = null;
    let reconnectTimerId = null;  // single in-flight reconnect timer

    // --- WebSocket ---

    function connect() {
        // Cancel any pending reconnect — back-to-back onclose events would
        // otherwise stack multiple timers and produce overlapping ws objects
        // (each with its own onmessage handler still attached).
        if (reconnectTimerId !== null) {
            clearTimeout(reconnectTimerId);
            reconnectTimerId = null;
        }
        // Detach handlers from any existing socket before replacing — old
        // sockets that were torn down racily could otherwise still deliver
        // a final message to the stale handler.
        if (ws) {
            ws.onopen = ws.onclose = ws.onerror = ws.onmessage = null;
            try { ws.close(); } catch (e) { /* ignore */ }
        }

        const proto = location.protocol === "https:" ? "wss:" : "ws:";
        ws = new WebSocket(`${proto}//${location.host}/ws`);
        window.ws = ws;
        liveStreamEl = null;  // reset streaming target on reconnect

        ws.onopen = function () {
            connStatus.textContent = "Connected";
            connStatus.className = "connected";
        };

        ws.onclose = function () {
            connStatus.textContent = "Disconnected — reconnecting...";
            connStatus.className = "disconnected";
            for (var key in taskStatuses) {
                if (taskStatuses[key] === "running") setTaskStatus(key, "failed");
            }
            // Schedule exactly one reconnect attempt; cancellation on the
            // next connect() entry guarantees no stacking.
            if (reconnectTimerId === null) {
                reconnectTimerId = setTimeout(function () {
                    reconnectTimerId = null;
                    connect();
                }, 2000);
            }
        };

        ws.onerror = function () {
            connStatus.textContent = "Connection error";
            connStatus.className = "disconnected";
        };

        ws.onmessage = function (event) {
            handleMessage(JSON.parse(event.data));
        };
    }

    function handleMessage(msg) {
        switch (msg.type) {
            case "connected":
                renderPipelineTree(msg.pipeline || []);
                var statuses = msg.phase_statuses || {};
                for (var phase in statuses) {
                    if (statuses[phase] !== "idle") setTaskStatus(phase, statuses[phase]);
                }
                var history = msg.history || [];
                for (var i = 0; i < history.length; i++) {
                    var h = history[i];
                    if (h.type === "agent_message") {
                        addLogMessage(h.agent, h.content, h.phase);
                    }
                }
                addSystemMessage("Connected to pipeline dashboard.");
                requestTypeRanges();
                break;

            case "agent_message":
                addLogMessage(msg.agent, msg.content, msg.phase);
                break;

            case "agent_stream":
                handleStream(msg.agent, msg.content, msg.phase);
                break;

            case "agent_user_message":
                addChatMessage("you", msg.text, "msg-user");
                showThinking();
                break;

            case "agent_response_chunk":
                clearThinking();
                appendClaudeStream(msg.text);
                break;

            case "agent_tool_request":
                clearThinking();
                // Finalize any in-flight claude bubble so its markdown
                // renders before the modal interrupts.
                finalizeClaudeStream();
                if (window.__showApprovalModal) {
                    window.__showApprovalModal(msg.request_id, msg.tool, msg.input);
                }
                break;

            case "agent_tool_use":
                clearThinking();
                // Same idea: render the bubble that preceded this tool
                // call so the user sees formatted text before tool output.
                finalizeClaudeStream();
                var statusCls = msg.status === "auto-allowed" ? "msg-tool-ok" : "msg-tool-pending";
                addChatMessage(
                    "tool",
                    msg.tool + " " + summarizeToolInput(msg.input),
                    statusCls
                );
                break;

            case "agent_tool_result":
                addChatMessage(
                    "tool",
                    msg.tool + " " + (msg.success ? "OK" : "FAILED")
                        + (msg.output_excerpt ? "\n" + truncate(msg.output_excerpt, 240) : ""),
                    msg.success ? "msg-tool-ok" : "msg-tool-error"
                );
                // Claude may be thinking again before the next stream chunk
                // (between tool calls). Show indicator until next event.
                showThinking();
                break;

            case "agent_done":
                clearThinking();
                finalizeClaudeStream();
                if (window.__setChatBusy) window.__setChatBusy(false);
                break;

            case "agent_error":
                clearThinking();
                addChatMessage("error", msg.message, "msg-tool-error");
                if (window.__setChatBusy) window.__setChatBusy(false);
                break;

            case "agent_aborted":
                clearThinking();
                addChatMessage("system", "Turn aborted.", "msg-tool-error");
                if (window.__setChatBusy) window.__setChatBusy(false);
                break;

            case "agent_new_chat_ack":
                addChatMessage("system", "Started a new chat.", "msg-user");
                break;

            case "phase_complete":
                setTaskStatus(msg.phase, msg.status);
                addSystemMessage('Phase "' + msg.phase + '" ' + msg.status + '.');
                if (msg.status === "failed" && msg.error) {
                    addSystemMessage("Error: " + msg.error, true);
                }
                if (selectedTask === msg.phase || !selectedTask) {
                    selectTask(msg.phase);
                }
                // Refresh feedback if this phase is currently displayed
                if (feedbackPhase === msg.phase) {
                    fetchFeedback(msg.phase);
                }
                // Refresh type ranges after M2T generates new CSP files
                if (msg.phase === "m2t" && msg.status === "completed") {
                    requestTypeRanges();
                }
                // Inline-render phase feedback in the chat (especially on
                // failure) so users don't have to switch panels.
                surfacePhaseFeedback(msg.phase, msg.status);
                break;

            case "error":
                addSystemMessage("Error: " + msg.message, true);
                break;

            case "file_list":
                renderFileList(msg.files);
                break;

            case "publish_result":
                publishBtn.disabled = false;
                publishBtn.textContent = msg.status === "ok"
                    ? msg.count + " published" : "Failed";
                setTimeout(function () { publishBtn.textContent = "Publish"; }, 2500);
                if (msg.status === "ok") {
                    addSystemMessage("Published " + msg.count + " file(s) to " + msg.target);
                } else {
                    addSystemMessage("Publish failed: " + msg.message, true);
                }
                break;

            case "clear_result":
                clearBtn.disabled = false;
                clearBtn.textContent = "Clear";
                if (msg.status === "ok") {
                    addSystemMessage("Cleared " + msg.count + " file(s).");
                    renderFileList([]);
                    filePreview.classList.remove("visible");
                    if (msg.task) {
                        setTaskStatus(msg.task, "idle");
                    } else {
                        for (var key in taskStatuses) setTaskStatus(key, "idle");
                    }
                } else {
                    addSystemMessage("Clear failed: " + msg.message, true);
                }
                break;

            case "reset_all_result":
                if (resetAllBtn) {
                    resetAllBtn.disabled = false;
                    resetAllBtn.textContent = "Reset";
                }
                if (msg.status === "ok") {
                    addSystemMessage(
                        "Reset complete — wiped " + msg.output_count +
                        " output file(s) and " + msg.feedback_count +
                        " feedback file(s); every phase status returned to idle."
                    );
                    renderFileList([]);
                    if (typeof filePreview !== "undefined" && filePreview) {
                        filePreview.classList.remove("visible");
                    }
                    for (var key in taskStatuses) setTaskStatus(key, "idle");
                } else {
                    addSystemMessage("Reset failed: " + msg.message, true);
                }
                break;

            case "clean_project_result":
                cleanProjectBtn.disabled = false;
                cleanProjectBtn.textContent = "Clean";
                if (msg.status === "ok") {
                    addSystemMessage("Cleaned project (" + msg.count + " file(s) removed).");
                } else {
                    addSystemMessage("Clean failed: " + msg.message, true);
                }
                break;

            case "fdr4_memory":
                updateFdr4Memory(msg.memory_mb, msg.peak_mb, msg.limit_mb);
                break;

            case "type_ranges":
                renderTypeRangesTable(msg.ranges, msg.saved);
                break;

            case "type_ranges_applied":
                if (msg.status === "ok") {
                    addSystemMessage("Type ranges applied.");
                    requestTypeRanges();
                } else {
                    addSystemMessage("Failed: " + msg.message, true);
                }
                break;

            case "type_ranges_reset":
                if (msg.status === "ok") {
                    addSystemMessage("Type ranges reset to defaults.");
                    requestTypeRanges();
                } else {
                    addSystemMessage("Failed: " + msg.message, true);
                }
                break;
        }
    }

    // --- Log rendering ---

    function handleStream(agent, content, phase) {
        if (!liveStreamEl) {
            liveStreamEl = document.createElement("div");
            liveStreamEl.className = "message";
            liveStreamEl.setAttribute("data-agent", agent);
            var nameEl = document.createElement("div");
            nameEl.className = "agent-name";
            nameEl.textContent = (phase || agent);
            liveStreamEl.appendChild(nameEl);
            var contentEl = document.createElement("div");
            contentEl.className = "content streaming-content";
            liveStreamEl.appendChild(contentEl);
            logMessages.appendChild(liveStreamEl);
        }
        var el = liveStreamEl.querySelector(".streaming-content");
        if (el.textContent) {
            el.textContent += "\n" + content;
        } else {
            el.textContent = content;
        }
        if (autoScroll) scrollToBottom();
    }

    function addLogMessage(agent, content, phase) {
        // Replace live stream element
        if (liveStreamEl) {
            liveStreamEl.remove();
            liveStreamEl = null;
        }

        if (!content || !content.trim()) return;

        var div = document.createElement("div");
        div.className = "message";
        div.setAttribute("data-agent", agent);

        var nameEl = document.createElement("div");
        nameEl.className = "agent-name";
        nameEl.textContent = (phase || agent);
        div.appendChild(nameEl);

        // Check for markdown tables (FDR4 results)
        var tableResult = renderMarkdownTable(content);
        if (tableResult) {
            if (tableResult.before.trim()) {
                var beforeEl = document.createElement("div");
                beforeEl.className = "content";
                beforeEl.textContent = tableResult.before.trim();
                div.appendChild(beforeEl);
            }
            var tableEl = document.createElement("div");
            tableEl.className = "content fdr4-table-wrapper";
            tableEl.innerHTML = tableResult.tableHtml;
            div.appendChild(tableEl);
            if (tableResult.after.trim()) {
                var afterEl = document.createElement("div");
                afterEl.className = "content";
                afterEl.textContent = tableResult.after.trim();
                div.appendChild(afterEl);
            }
        } else {
            var contentEl = document.createElement("div");
            contentEl.className = "content";
            contentEl.textContent = content;
            div.appendChild(contentEl);
        }

        logMessages.appendChild(div);
        if (autoScroll) scrollToBottom();
    }

    function addSystemMessage(text, isError) {
        var div = document.createElement("div");
        div.className = "message";
        div.setAttribute("data-agent", "system");

        var nameEl = document.createElement("div");
        nameEl.className = "agent-name";
        nameEl.textContent = "system";

        var contentEl = document.createElement("div");
        contentEl.className = "content";
        contentEl.textContent = text;
        if (isError) contentEl.style.color = "var(--error)";

        div.appendChild(nameEl);
        div.appendChild(contentEl);
        logMessages.appendChild(div);
        if (autoScroll) scrollToBottom();
    }

    function renderMarkdownTable(text) {
        var lines = text.split("\n");
        var tableStart = -1, tableEnd = -1;
        for (var i = 0; i < lines.length; i++) {
            var trimmed = lines[i].trim();
            if (/^\|.+\|$/.test(trimmed)) {
                if (tableStart === -1) tableStart = i;
                tableEnd = i;
            } else if (tableStart !== -1) break;
        }
        if (tableStart === -1 || tableEnd - tableStart < 2) return null;

        var separatorLine = lines[tableStart + 1].trim();
        if (!/^\|[\s\-|]+\|$/.test(separatorLine)) return null;

        var headers = lines[tableStart].trim().split("|").filter(function (c) { return c.trim(); })
            .map(function (c) { return c.trim(); });

        function esc(s) {
            var d = document.createElement("span");
            d.textContent = s;
            return d.innerHTML;
        }

        var html = '<table class="fdr4-results"><thead><tr>';
        for (var hi = 0; hi < headers.length; hi++) html += "<th>" + esc(headers[hi]) + "</th>";
        html += "</tr></thead><tbody>";

        for (var ri = tableStart + 2; ri <= tableEnd; ri++) {
            var cells = lines[ri].trim().split("|").filter(function (c) { return c.trim(); })
                .map(function (c) { return c.trim(); });
            html += "<tr>";
            for (var ci = 0; ci < cells.length; ci++) {
                var cell = cells[ci];
                if (/^PASS/.test(cell)) html += '<td class="fdr4-pass"><strong>' + esc(cell) + "</strong></td>";
                else if (/^EXPECTED FAIL/.test(cell)) html += '<td class="fdr4-expected"><strong>' + esc(cell) + "</strong></td>";
                else if (/^FAIL/.test(cell)) html += '<td class="fdr4-fail"><strong>' + esc(cell) + "</strong></td>";
                else html += "<td>" + esc(cell) + "</td>";
            }
            html += "</tr>";
        }
        html += "</tbody></table>";

        return {
            before: lines.slice(0, tableStart).join("\n"),
            tableHtml: html,
            after: lines.slice(tableEnd + 1).join("\n"),
        };
    }

    function isNearBottom() {
        return logMessages.scrollHeight - logMessages.clientHeight
               <= logMessages.scrollTop + 40;
    }

    function scrollToBottom() {
        logMessages.scrollTop = logMessages.scrollHeight;
        // Programmatic scroll doesn't fire 'scroll' on every browser; sync
        // autoScroll explicitly so subsequent autoSize-driven changes don't
        // get out of step with the listener-based flag.
        autoScroll = true;
    }

    // Update autoScroll on user scroll AND whenever DOM mutates inside
    // logMessages — adding/removing children (e.g. liveStreamEl.remove()
    // during streaming) shrinks scrollHeight, leaving the listener-only
    // flag stale: the user could have been near the bottom by virtue of the
    // shrink but autoScroll still reads false.
    logMessages.addEventListener("scroll", function () {
        autoScroll = isNearBottom();
    });
    new MutationObserver(function () {
        // Recompute on mutate; if the user is now within the
        // 40px threshold we should resume auto-following.
        if (isNearBottom()) autoScroll = true;
    }).observe(logMessages, { childList: true, subtree: true });

    // --- Pipeline tree ---

    function renderPipelineTree(pipeline) {
        pipelineTree.innerHTML = "";
        for (var i = 0; i < pipeline.length; i++) {
            pipelineTree.appendChild(createPhaseNode(pipeline[i]));
        }
    }

    function createPhaseNode(phase) {
        var node = document.createElement("div");
        node.className = "tree-phase";
        node.id = "phase-" + phase.id;

        var header = document.createElement("div");
        header.className = "tree-phase-header";

        var hasCommand = !!phase.command;
        var isInteractive = phase.id === "requirements"
            || phase.id === "code_synthesis"
            || phase.id === "refinement";
        var isFdr4 = phase.command === "fdr4";

        var arrow = document.createElement("span");
        arrow.className = isFdr4 ? "arrow" : "arrow hidden";
        arrow.textContent = "\u25B6";

        var label = document.createElement("span");
        label.className = "phase-label";
        label.textContent = phase.label;
        if (!hasCommand && !isInteractive) label.classList.add("no-tasks");
        if (isInteractive) label.classList.add("interactive");

        var dot = document.createElement("span");
        dot.className = "status-dot";
        if (hasCommand) {
            dot.id = "status-" + phase.command;
        } else {
            dot.style.visibility = "hidden";
        }

        header.appendChild(arrow);
        header.appendChild(label);
        header.appendChild(dot);

        // Interactive phases (no `command`, no Start button) get a click
        // handler that just applies the .selected visual focus so the
        // user gets feedback that the row is acknowledged. Requirements
        // additionally reveals + opens the right-panel Requirements tab;
        // other interactive phases hide it.
        if (isInteractive) {
            header.id = "task-" + phase.id;
            label.style.cursor = "pointer";
            label.addEventListener("click", function (e) {
                e.stopPropagation();
                var prev = document.querySelector(".tree-phase-header.selected");
                if (prev) prev.classList.remove("selected");
                header.classList.add("selected");
                selectedTask = phase.id;
                setContextualTabs(phase.id);
                if (phase.id === "requirements") {
                    activateTab("requirements");
                } else if (phase.id === "code_synthesis") {
                    activateTab("prompts");
                } else if (phase.id === "refinement") {
                    activateTab("commands");
                    loadCommands();
                }
            });
        }

        if (hasCommand) {
            header.id = "task-" + phase.command;
            label.style.cursor = "pointer";
            label.addEventListener("click", function (e) {
                e.stopPropagation();
                selectTask(phase.command);
            });

            var actions = document.createElement("span");
            actions.className = "phase-actions";

            var startBtn = document.createElement("button");
            startBtn.className = "task-btn start-btn";
            startBtn.textContent = "Start";
            startBtn.addEventListener("click", function (e) {
                e.stopPropagation();
                startCommand(phase.command);
            });

            var stopBtn = document.createElement("button");
            stopBtn.className = "task-btn stop-btn";
            stopBtn.textContent = "Stop";
            stopBtn.addEventListener("click", function (e) {
                e.stopPropagation();
                stopPhase();
            });

            actions.appendChild(startBtn);
            actions.appendChild(stopBtn);
            header.appendChild(actions);
        }

        var children = document.createElement("div");
        children.className = "tree-phase-children";

        // FDR4: type ranges panel + memory monitor
        if (isFdr4) {
            arrow.className = "arrow expanded";
            children.classList.add("expanded");
            children.appendChild(createTypeRangesPanel());
            children.appendChild(createFdr4MemoryMonitor());

            header.addEventListener("click", function () {
                arrow.classList.toggle("expanded");
                children.classList.toggle("expanded");
            });
        }

        node.appendChild(header);
        node.appendChild(children);
        return node;
    }

    // --- Type Ranges (FDR4) ---

    function createTypeRangesPanel() {
        var panel = document.createElement("div");
        panel.className = "type-ranges-panel";
        panel.id = "type-ranges-panel";

        var title = document.createElement("div");
        title.className = "type-ranges-title";
        title.textContent = "Type Ranges";
        panel.appendChild(title);

        var table = document.createElement("table");
        table.className = "type-ranges-table";
        table.id = "type-ranges-table";
        table.innerHTML =
            "<thead><tr><th>Type</th><th>CSP Name</th><th>Lower</th><th>Upper</th></tr></thead>" +
            "<tbody><tr><td colspan='4' class='type-ranges-loading'>Loading...</td></tr></tbody>";
        panel.appendChild(table);

        var actions = document.createElement("div");
        actions.className = "type-ranges-actions";

        var applyBtn = document.createElement("button");
        applyBtn.className = "task-btn type-ranges-apply-btn";
        applyBtn.textContent = "Apply";
        applyBtn.addEventListener("click", function () { applyTypeRanges(); });

        var resetBtn = document.createElement("button");
        resetBtn.className = "task-btn type-ranges-reset-btn";
        resetBtn.textContent = "Reset";
        resetBtn.addEventListener("click", function () { resetTypeRanges(); });

        actions.appendChild(applyBtn);
        actions.appendChild(resetBtn);
        panel.appendChild(actions);

        requestTypeRanges();
        return panel;
    }

    function requestTypeRanges() {
        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify({ type: "get_type_ranges" }));
        }
    }

    function renderTypeRangesTable(ranges, saved) {
        var table = document.getElementById("type-ranges-table");
        if (!table) return;
        var tbody = table.querySelector("tbody");
        if (!tbody) return;
        tbody.innerHTML = "";

        if (!ranges || ranges.length === 0) {
            tbody.innerHTML = "<tr><td colspan='4' class='type-ranges-loading'>No ranges. Run M2T first.</td></tr>";
            return;
        }

        for (var i = 0; i < ranges.length; i++) {
            var r = ranges[i];
            var savedR = saved && saved[r.name];
            var lo = savedR ? savedR.lower : r.lower;
            var hi = savedR ? savedR.upper : r.upper;

            // Build via DOM so r.name / r.csp_name (which trace back to user
            // Java identifiers via the M2T pipeline) cannot inject markup or
            // break out of attribute values.
            var tr = document.createElement("tr");

            var tdName = document.createElement("td");
            tdName.textContent = r.name == null ? "" : String(r.name);
            tr.appendChild(tdName);

            var tdCsp = document.createElement("td");
            tdCsp.className = "type-ranges-csp";
            tdCsp.textContent = r.csp_name == null ? "" : String(r.csp_name);
            tr.appendChild(tdCsp);

            ["lower", "upper"].forEach(function (bound) {
                var td = document.createElement("td");
                var inp = document.createElement("input");
                inp.type = "number";
                inp.className = "type-range-input";
                inp.setAttribute("data-name", r.name == null ? "" : String(r.name));
                inp.setAttribute("data-bound", bound);
                inp.value = (bound === "lower" ? lo : hi);
                td.appendChild(inp);
                tr.appendChild(td);
            });

            tbody.appendChild(tr);
        }
    }

    function applyTypeRanges() {
        var inputs = document.querySelectorAll(".type-range-input");
        var ranges = {};
        for (var i = 0; i < inputs.length; i++) {
            var inp = inputs[i];
            var name = inp.getAttribute("data-name");
            var bound = inp.getAttribute("data-bound");
            if (!ranges[name]) ranges[name] = {};
            ranges[name][bound] = parseInt(inp.value, 10) || 0;
        }
        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify({ type: "apply_type_ranges", ranges: ranges }));
        }
    }

    function resetTypeRanges() {
        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify({ type: "reset_type_ranges" }));
        }
    }

    // --- FDR4 Memory Monitor ---

    function createFdr4MemoryMonitor() {
        var panel = document.createElement("div");
        panel.className = "fdr4-memory-panel";
        panel.id = "fdr4-memory-panel";
        panel.style.display = "none";

        var label = document.createElement("div");
        label.className = "fdr4-memory-label";
        label.textContent = "FDR4 Memory";
        panel.appendChild(label);

        var barWrap = document.createElement("div");
        barWrap.className = "fdr4-memory-bar-wrap";

        var bar = document.createElement("div");
        bar.className = "fdr4-memory-bar";
        bar.id = "fdr4-memory-bar";
        barWrap.appendChild(bar);
        panel.appendChild(barWrap);

        var info = document.createElement("div");
        info.className = "fdr4-memory-info";
        info.id = "fdr4-memory-info";
        info.textContent = "\u2014";
        panel.appendChild(info);

        return panel;
    }

    function updateFdr4Memory(memoryMb, peakMb, limitMb) {
        var panel = document.getElementById("fdr4-memory-panel");
        if (!panel) return;

        if (memoryMb === 0) {
            // Process ended — show peak then fade out
            var info = document.getElementById("fdr4-memory-info");
            if (info) info.textContent = "Done \u2014 peak: " + peakMb + " MB / " + limitMb + " MB";
            var bar = document.getElementById("fdr4-memory-bar");
            if (bar) {
                bar.style.width = Math.min(100, (peakMb / limitMb) * 100).toFixed(1) + "%";
                bar.className = "fdr4-memory-bar";
            }
            return;
        }

        panel.style.display = "";
        var pct = Math.min(100, (memoryMb / limitMb) * 100);
        var bar = document.getElementById("fdr4-memory-bar");
        if (bar) {
            bar.style.width = pct.toFixed(1) + "%";
            bar.className = "fdr4-memory-bar" +
                (pct > 80 ? " danger" : pct > 50 ? " warning" : "");
        }
        var info = document.getElementById("fdr4-memory-info");
        if (info) {
            info.textContent = memoryMb + " MB / " + limitMb + " MB" +
                " (peak: " + peakMb + " MB)";
        }
    }

    // --- Task selection & status ---

    // Contextual tabs — only visible for the matching interactive phase.
    // Requirements ↔ phase 1, Prompts ↔ phase 2. Hide when any other
    // phase is selected; fall back to Output Files if the user was on a
    // tab that's about to disappear.
    function setContextualTabs(phaseId) {
        var contextMap = {
            "tab-requirements": phaseId === "requirements",
            "tab-prompts":      phaseId === "code_synthesis",
            "tab-source":       phaseId === "code_synthesis",
            "tab-commands":     phaseId === "refinement",
        };
        // Phase 2 owns its own output view (Source), so Output Files is
        // redundant while phase 2 is selected — hide it then. Phase 7
        // (refinement) is a pure command-launcher view, so hide Files
        // and Feedback there too — neither applies.
        var hideFiles = phaseId === "code_synthesis" || phaseId === "refinement";
        var hideFeedback = phaseId === "refinement";
        var filesTab = document.getElementById("tab-files");
        var feedbackTab = document.getElementById("tab-feedback");
        if (filesTab) filesTab.style.display = hideFiles ? "none" : "";
        if (feedbackTab) feedbackTab.style.display = hideFeedback ? "none" : "";

        var fallback = false;
        Object.keys(contextMap).forEach(function (id) {
            var tab = document.getElementById(id);
            if (!tab) return;
            var show = contextMap[id];
            tab.style.display = show ? "" : "none";
            if (!show && tab.classList.contains("active")) fallback = true;
        });
        if (hideFiles && filesTab && filesTab.classList.contains("active")) {
            fallback = true;
        }
        if (hideFeedback && feedbackTab && feedbackTab.classList.contains("active")) {
            fallback = true;
        }
        if (fallback) {
            // Prefer the phase's primary contextual tab over plain Files.
            if (phaseId === "requirements") activateTab("requirements");
            else if (phaseId === "code_synthesis") activateTab("prompts");
            else if (phaseId === "refinement") activateTab("commands");
            else activateTab("files");
        }
    }
    function setRequirementsTabVisible(visible) {
        // Back-compat shim — selectTask still calls this.
        setContextualTabs(visible ? "requirements" : "");
    }

    function selectTask(taskId) {
        // Deselect previous
        var prev = document.querySelector(".tree-phase-header.selected");
        if (prev) prev.classList.remove("selected");

        selectedTask = taskId;
        setRequirementsTabVisible(taskId === "requirements");

        // Highlight
        var header = document.getElementById("task-" + taskId);
        if (header) header.classList.add("selected");

        // Request file list
        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify({ type: "list_files", task: taskId }));
        }

        // Load feedback for this phase (populates the Feedback tab)
        fetchFeedback(taskId);
    }

    function setTaskStatus(taskId, status) {
        taskStatuses[taskId] = status;
        var dot = document.getElementById("status-" + taskId);
        if (dot) {
            dot.className = "status-dot";
            if (status !== "idle") dot.classList.add(status);
        }
        var phaseNode = document.getElementById("phase-" + taskId);
        if (phaseNode) {
            phaseNode.classList.remove("running");
            if (status === "running") phaseNode.classList.add("running");
        }
        updateWorkingIndicator();
    }

    function isAnyPhaseRunning() {
        for (var key in taskStatuses) {
            if (taskStatuses[key] === "running") return true;
        }
        return false;
    }

    function updateWorkingIndicator() {
        if (isAnyPhaseRunning()) {
            workingIndicator.classList.add("visible");
            if (!workingTimerId) {
                workingStartTime = Date.now();
                workingTimerId = setInterval(function () {
                    var elapsed = Math.floor((Date.now() - workingStartTime) / 1000);
                    var min = Math.floor(elapsed / 60);
                    var sec = elapsed % 60;
                    workingText.textContent = "Running... " +
                        (min > 0 ? min + "m " : "") + sec + "s";
                }, 1000);
            }
        } else {
            workingIndicator.classList.remove("visible");
            if (workingTimerId) {
                clearInterval(workingTimerId);
                workingTimerId = null;
                workingStartTime = null;
            }
            // Finalize live stream
            if (liveStreamEl) {
                liveStreamEl = null;
            }
        }
    }

    // --- Commands ---

    function startCommand(commandId) {
        if (ws && ws.readyState === WebSocket.OPEN) {
            setTaskStatus(commandId, "running");
            ws.send(JSON.stringify({ type: "start_command", command: commandId }));
        }
    }

    function stopPhase() {
        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify({ type: "stop_phase" }));
        }
    }

    // --- Feedback panel ---

    let feedbackPhase = null;  // currently displayed phase in the Feedback tab

    if (feedbackCopyBtn) {
        feedbackCopyBtn.addEventListener("click", function () {
            var titleText = (feedbackTitle && feedbackTitle.textContent) || "";
            var subtitleText = (feedbackSubtitle && feedbackSubtitle.textContent) || "";
            var bodyText = (feedbackBody && feedbackBody.innerText) || "";
            var combined = (titleText + "\n" + subtitleText + "\n\n" + bodyText).trim();
            var done = function () {
                feedbackCopyBtn.textContent = "Copied";
                feedbackCopyBtn.classList.add("copied");
                setTimeout(function () {
                    feedbackCopyBtn.textContent = "Copy";
                    feedbackCopyBtn.classList.remove("copied");
                }, 1200);
            };
            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(combined).then(done, function () {
                    fallbackCopy(combined); done();
                });
            } else {
                fallbackCopy(combined); done();
            }
        });
    }

    function fallbackCopy(text) {
        var ta = document.createElement("textarea");
        ta.value = text;
        ta.style.position = "fixed";
        ta.style.opacity = "0";
        document.body.appendChild(ta);
        ta.select();
        try { document.execCommand("copy"); } catch (e) { /* ignore */ }
        document.body.removeChild(ta);
    }

    function phaseLabelFor(id) {
        // Look up a phase's UI label from the tree
        var node = document.getElementById("task-" + id);
        if (!node) return id;
        var lbl = node.querySelector(".phase-label");
        return lbl ? lbl.textContent : id;
    }

    function renderFeedback(phase, data) {
        feedbackPhase = phase;
        feedbackTitle.textContent = "Feedback: " + phaseLabelFor(phase);
        feedbackSubtitle.textContent = data.summary || "";
        feedbackBody.innerHTML = "";

        // Status badge on its own row, summary in a block beneath it
        var statusRow = document.createElement("div");
        statusRow.className = "feedback-status-row";
        var statusSpan = document.createElement("span");
        statusSpan.className = "feedback-status " + (data.status || "");
        statusSpan.textContent = (data.status || "").toUpperCase() || "—";
        statusRow.appendChild(statusSpan);
        feedbackBody.appendChild(statusRow);
        var summary = document.createElement("div");
        summary.className = "feedback-summary";
        summary.textContent = data.summary || "";
        feedbackBody.appendChild(summary);

        // Thrashing banner (any issue with seen_in_runs >= 3)
        var thrashing = (data.issues || []).filter(function (i) {
            return (i.seen_in_runs || 1) >= 3;
        });
        if (thrashing.length > 0) {
            var banner = document.createElement("div");
            banner.className = "feedback-thrashing";
            var titles = thrashing.map(function (i) {
                return "'" + i.title + "'";
            }).join(", ");
            banner.innerHTML =
                "<strong>WARNING — thrashing detected.</strong> " +
                thrashing.length + " issue(s) have recurred 3+ runs in a row: " +
                escapeHtml(titles) +
                ". Previous fix strategies are not working — change approach " +
                "(re-read the requirement, or escalate to the user).";
            feedbackBody.appendChild(banner);
        }

        // Diff chips (new / recurring / resolved counts)
        var hasDiff = (data.new_count || 0) + (data.recurring_count || 0) +
                      (data.resolved_count || 0) > 0;
        if (hasDiff) {
            var diff = document.createElement("div");
            diff.className = "feedback-diff";
            [
                ["new", "New", data.new_count || 0],
                ["recurring", "Recurring", data.recurring_count || 0],
                ["resolved", "Resolved", data.resolved_count || 0],
            ].forEach(function (entry) {
                var chip = document.createElement("span");
                chip.className = "diff-chip " + entry[0];
                chip.textContent = entry[1] + ": " + entry[2];
                diff.appendChild(chip);
            });
            feedbackBody.appendChild(diff);
        }

        // Resolved-issue titles
        if (data.resolved_issue_titles && data.resolved_issue_titles.length > 0) {
            var resolved = document.createElement("div");
            resolved.className = "feedback-resolved-list";
            resolved.innerHTML = "<strong>Resolved since previous run:</strong> " +
                data.resolved_issue_titles
                    .map(function (t) { return escapeHtml(t); })
                    .join("; ");
            feedbackBody.appendChild(resolved);
        }

        // Issues
        var issues = data.issues || [];
        if (issues.length === 0 && (data.status === "passed" || !hasDiff)) {
            var none = document.createElement("div");
            none.className = "feedback-empty";
            none.textContent = data.status === "passed"
                ? "No issues — clean pass."
                : "No issues reported.";
            feedbackBody.appendChild(none);
        } else {
            for (var i = 0; i < issues.length; i++) {
                feedbackBody.appendChild(renderIssueCard(issues[i], i + 1));
            }
        }

        // Files to review
        if (data.files_to_review && data.files_to_review.length > 0) {
            var ftr = document.createElement("div");
            ftr.className = "feedback-files-to-review";
            var lbl = document.createElement("span");
            lbl.className = "label";
            lbl.textContent = "Files to review";
            ftr.appendChild(lbl);
            var ul = document.createElement("ul");
            ul.style.listStyle = "none";
            ul.style.marginTop = "4px";
            data.files_to_review.forEach(function (f) {
                var li = document.createElement("li");
                var reqs = (f.requirement_ids || []).join(", ");
                li.textContent = "• " + f.file + (reqs ? " (" + reqs + ")" : "");
                ul.appendChild(li);
            });
            ftr.appendChild(ul);
            feedbackBody.appendChild(ftr);
        }

        // Next step
        if (data.next_step) {
            var ns = document.createElement("div");
            ns.className = "feedback-next-step";
            var lbl2 = document.createElement("span");
            lbl2.className = "label";
            lbl2.textContent = "Next step";
            ns.appendChild(lbl2);
            var text = document.createElement("div");
            text.textContent = data.next_step;
            ns.appendChild(text);
            feedbackBody.appendChild(ns);
        }

        updateFeedbackTabBadge(data);
    }

    function renderIssueCard(issue, idx) {
        var card = document.createElement("div");
        card.className = "feedback-issue";
        var seen = issue.seen_in_runs || 1;
        if (seen >= 3) card.classList.add("thrashing");

        var header = document.createElement("div");
        header.className = "feedback-issue-header";
        var kind = document.createElement("span");
        kind.className = "feedback-issue-kind";
        kind.textContent = issue.kind || "issue";
        header.appendChild(kind);
        var title = document.createElement("span");
        title.className = "feedback-issue-title";
        title.textContent = "#" + idx + ". " + (issue.title || "");
        header.appendChild(title);
        if (seen >= 3) {
            var badge = document.createElement("span");
            badge.className = "feedback-issue-badge thrashing";
            badge.textContent = "RECURRING x" + seen;
            header.appendChild(badge);
        } else if (seen >= 2) {
            var badge2 = document.createElement("span");
            badge2.className = "feedback-issue-badge";
            badge2.textContent = "recurring x" + seen;
            header.appendChild(badge2);
        }
        card.appendChild(header);

        var body = document.createElement("div");
        body.className = "feedback-issue-body";

        if (issue.raw) {
            var rawLbl = document.createElement("div");
            rawLbl.className = "feedback-section-label";
            rawLbl.textContent = "Raw";
            body.appendChild(rawLbl);
            var raw = document.createElement("div");
            raw.className = "feedback-raw";
            raw.textContent = issue.raw;
            body.appendChild(raw);
        }

        if (issue.java_trace && issue.java_trace.length > 0) {
            var traceLbl = document.createElement("div");
            traceLbl.className = "feedback-section-label";
            traceLbl.textContent = "Java trace";
            body.appendChild(traceLbl);
            issue.java_trace.forEach(function (ref) {
                body.appendChild(renderTraceRef(ref));
            });
        }

        if (issue.fix_directive) {
            var fixLbl = document.createElement("div");
            fixLbl.className = "feedback-section-label";
            fixLbl.textContent = "Fix directive";
            body.appendChild(fixLbl);
            var fix = document.createElement("div");
            fix.className = "feedback-directive";
            fix.textContent = issue.fix_directive;
            body.appendChild(fix);
        }

        card.appendChild(body);
        return card;
    }

    function renderTraceRef(ref) {
        var line = document.createElement("div");
        line.className = "feedback-trace";
        var bits = [];
        if (ref.robochart_type || ref.robochart_element) {
            bits.push('<span class="trace-meta">' +
                escapeHtml(ref.robochart_type || "") + " " +
                escapeHtml(ref.robochart_element || "") +
                "</span>");
        }
        if (ref.file) {
            var loc = shortName(ref.file);
            if (ref.line_start) loc += ":" + ref.line_start;
            if (ref.line_end && ref.line_end !== ref.line_start) loc += "-" + ref.line_end;
            bits.push("<strong>" + escapeHtml(loc) + "</strong>");
        }
        if (ref.element) {
            bits.push('<span class="trace-meta">(' + escapeHtml(ref.element) + ")</span>");
        }
        if (ref.requirement_ids && ref.requirement_ids.length > 0) {
            bits.push('<span class="trace-meta">[' +
                escapeHtml(ref.requirement_ids.join(", ")) + "]</span>");
        }
        line.innerHTML = "→ " + bits.join(" ");
        return line;
    }

    function shortName(p) {
        if (!p) return "";
        var parts = p.replace(/\\/g, "/").split("/");
        return parts[parts.length - 1];
    }

    function escapeHtml(s) {
        var d = document.createElement("span");
        d.textContent = s == null ? "" : String(s);
        return d.innerHTML;
    }

    function updateFeedbackTabBadge(data) {
        var tab = document.getElementById("tab-feedback");
        if (!tab) return;
        // Remove prior badge
        var prev = tab.querySelector(".tab-badge");
        if (prev) prev.remove();
        var badge = document.createElement("span");
        badge.className = "tab-badge";
        var issues = (data && data.issues) || [];
        var thrashing = issues.filter(function (i) {
            return (i.seen_in_runs || 1) >= 3;
        }).length;
        if (thrashing > 0) {
            badge.textContent = "!" + thrashing;
            badge.classList.add("warn");
        } else if (issues.length > 0) {
            badge.textContent = issues.length;
        } else if (data && data.status === "passed") {
            badge.textContent = "OK";
            badge.classList.add("ok");
        } else {
            return; // no badge
        }
        tab.appendChild(badge);
    }

    // Monotonic fetch token — protects against out-of-order resolution
    // when the user switches phases mid-flight (or when phase_complete
    // and selectTask both fire fetchFeedback for the same/different
    // phases). The handler ignores any response whose token is no
    // longer the latest.
    var feedbackFetchToken = 0;

    function fetchFeedback(phase) {
        if (!phase) return;
        var myToken = ++feedbackFetchToken;
        var myPhase = phase;
        fetch("/api/feedback/" + encodeURIComponent(phase))
            .then(function (resp) {
                if (myToken !== feedbackFetchToken) return null;  // stale
                if (resp.status === 404) {
                    showFeedbackEmpty(phase, "No feedback yet — run this phase to generate feedback.");
                    return null;
                }
                return resp.json();
            })
            .then(function (data) {
                if (myToken !== feedbackFetchToken) return;  // stale
                if (data && !data.error) renderFeedback(myPhase, data);
            })
            .catch(function (err) {
                if (myToken !== feedbackFetchToken) return;  // stale
                showFeedbackEmpty(phase, "Failed to load feedback: " + err);
            });
    }

    function showFeedbackEmpty(phase, message) {
        feedbackPhase = phase;
        feedbackTitle.textContent = "Feedback: " + phaseLabelFor(phase);
        feedbackSubtitle.textContent = "";
        feedbackBody.innerHTML = "";
        var empty = document.createElement("div");
        empty.className = "feedback-empty";
        empty.textContent = message;
        feedbackBody.appendChild(empty);
        // Clear tab badge
        var tab = document.getElementById("tab-feedback");
        if (tab) {
            var b = tab.querySelector(".tab-badge");
            if (b) b.remove();
        }
    }

    // Tab switching
    function activateTab(name) {
        document.querySelectorAll(".panel-tab").forEach(function (btn) {
            btn.classList.toggle("active", btn.getAttribute("data-tab") === name);
        });
        document.querySelectorAll(".panel-view").forEach(function (view) {
            view.classList.toggle("active", view.id === "view-" + name);
        });
    }
    document.querySelectorAll(".panel-tab").forEach(function (btn) {
        btn.addEventListener("click", function () {
            activateTab(btn.getAttribute("data-tab"));
        });
    });

    // --- File browser ---

    function renderFileList(files) {
        fileList.innerHTML = "";
        if (!files || !files.length) {
            var empty = document.createElement("div");
            empty.className = "file-item";
            empty.style.color = "var(--text-dim)";
            empty.style.fontStyle = "italic";
            empty.textContent = "No output files";
            fileList.appendChild(empty);
            return;
        }

        for (var i = 0; i < files.length; i++) {
            (function (filePath) {
                var item = document.createElement("div");
                item.className = "file-item";
                // Show short name
                var parts = filePath.replace("@ext/", "").split("/");
                item.textContent = parts[parts.length - 1];
                item.title = filePath;
                item.addEventListener("click", function () {
                    loadFilePreview(filePath);
                });
                fileList.appendChild(item);
            })(files[i]);
        }
    }

    function loadFilePreview(filePath) {
        var apiPath;
        if (filePath.startsWith("@ext/")) {
            apiPath = "/api/ext-files/" + filePath.slice(5);
        } else {
            apiPath = "/api/files/" + filePath;
        }

        fetch(apiPath)
            .then(function (resp) { return resp.json(); })
            .then(function (data) {
                if (data.error) {
                    previewHeader.textContent = "Error: " + data.error;
                    previewCode.textContent = "";
                } else {
                    previewHeader.textContent = data.path;
                    previewCode.textContent = data.content;
                    previewCode.className = "";
                    // hljs >=11 sets data-highlighted on processed nodes;
                    // subsequent highlightElement() calls become no-ops if
                    // this attribute persists, so a previously-highlighted
                    // element keeps its OLD language. Clear it explicitly.
                    if (previewCode.dataset) {
                        delete previewCode.dataset.highlighted;
                    }

                    // Detect language for syntax highlighting
                    var ext = ((data.path || "").split(".").pop() || "").toLowerCase();
                    var langMap = {
                        java: "java", csp: "haskell", dfy: "csharp",
                        json: "json", xmi: "xml", rct: "haskell",
                    };
                    if (langMap[ext]) {
                        previewCode.className = "language-" + langMap[ext];
                    }
                    if (typeof hljs !== "undefined") {
                        hljs.highlightElement(previewCode);
                    }
                }
                filePreview.classList.add("visible");
            })
            .catch(function (err) {
                previewHeader.textContent = "Failed to load file";
                previewCode.textContent = String(err);
                filePreview.classList.add("visible");
            });
    }

    // --- Publish / Clear / Clean ---

    publishBtn.addEventListener("click", function () {
        if (ws && ws.readyState === WebSocket.OPEN) {
            publishBtn.disabled = true;
            publishBtn.textContent = "Publishing...";
            ws.send(JSON.stringify({ type: "publish" }));
        }
    });

    var clearConfirmTimer = null;
    clearBtn.addEventListener("click", function () {
        if (clearBtn.classList.contains("confirming")) {
            clearTimeout(clearConfirmTimer);
            clearBtn.classList.remove("confirming");
            if (ws && ws.readyState === WebSocket.OPEN) {
                clearBtn.disabled = true;
                clearBtn.textContent = "Clearing...";
                var msg = { type: "clear_output" };
                if (selectedTask) msg.task = selectedTask;
                ws.send(JSON.stringify(msg));
            }
        } else {
            clearBtn.textContent = selectedTask ? "Clear " + selectedTask + "?" : "Clear all?";
            clearBtn.classList.add("confirming");
            clearConfirmTimer = setTimeout(function () {
                clearBtn.textContent = "Clear";
                clearBtn.classList.remove("confirming");
            }, 3000);
        }
    });

    // Reset-everything button (header). Two-click confirmation pattern
    // matches the existing Clear / Clean buttons. Wipes both the t2m
    // output dir and the post_*.{md,json} feedback files, then resets
    // every phase status to idle.
    var resetAllConfirmTimer = null;
    if (resetAllBtn) {
        resetAllBtn.addEventListener("click", function () {
            if (resetAllBtn.classList.contains("confirming")) {
                clearTimeout(resetAllConfirmTimer);
                resetAllBtn.classList.remove("confirming");
                if (ws && ws.readyState === WebSocket.OPEN) {
                    resetAllBtn.disabled = true;
                    resetAllBtn.textContent = "Resetting...";
                    ws.send(JSON.stringify({ type: "reset_all" }));
                }
            } else {
                resetAllBtn.textContent = "Wipe output + feedback?";
                resetAllBtn.classList.add("confirming");
                resetAllConfirmTimer = setTimeout(function () {
                    resetAllBtn.textContent = "Reset";
                    resetAllBtn.classList.remove("confirming");
                }, 3000);
            }
        });
    }

    var cleanConfirmTimer = null;
    cleanProjectBtn.addEventListener("click", function () {
        if (cleanProjectBtn.classList.contains("confirming")) {
            clearTimeout(cleanConfirmTimer);
            cleanProjectBtn.classList.remove("confirming");
            if (ws && ws.readyState === WebSocket.OPEN) {
                cleanProjectBtn.disabled = true;
                cleanProjectBtn.textContent = "Cleaning...";
                ws.send(JSON.stringify({ type: "clean_project" }));
            }
        } else {
            cleanProjectBtn.textContent = "Confirm?";
            cleanProjectBtn.classList.add("confirming");
            cleanConfirmTimer = setTimeout(function () {
                cleanProjectBtn.textContent = "Clean";
                cleanProjectBtn.classList.remove("confirming");
            }, 3000);
        }
    });

    // --- Resize handles ---

    function initResize(handleId, targetId, anchor) {
        // anchor = "left" → target grows with rightward drag (default).
        // anchor = "right" → target is right-anchored (fixed-width pane
        // on the right); rightward drag shrinks it, leftward grows it.
        var handle = document.getElementById(handleId);
        var target = document.getElementById(targetId);
        if (!handle || !target) return;
        anchor = anchor || "left";

        var startX = 0;
        var startWidth = 0;
        var pointerId = null;

        function endDrag() {
            document.body.classList.remove("resizing");
            handle.classList.remove("dragging");
            if (pointerId !== null) {
                try { handle.releasePointerCapture(pointerId); } catch (e) { /* ignore */ }
                pointerId = null;
            }
        }

        handle.addEventListener("pointerdown", function (e) {
            if (e.button !== 0) return;
            e.preventDefault();
            startX = e.clientX;
            startWidth = target.offsetWidth;
            pointerId = e.pointerId;
            try { handle.setPointerCapture(pointerId); } catch (err) { /* ignore */ }
            document.body.classList.add("resizing");
            handle.classList.add("dragging");
        });

        handle.addEventListener("pointermove", function (e) {
            if (pointerId === null || e.pointerId !== pointerId) return;
            var dx = e.clientX - startX;
            var sign = anchor === "right" ? -1 : 1;
            target.style.width = Math.max(150, startWidth + sign * dx) + "px";
        });

        handle.addEventListener("pointerup", endDrag);
        handle.addEventListener("pointercancel", endDrag);
        window.addEventListener("blur", endDrag);
    }

    initResize("resize-left", "pipeline-sidebar", "left");
    initResize("resize-right", "file-panel", "right");

    // --- Init ---
    connect();

})();

// ─────────────────────────────────────────────────────────────────────
// Claude chat input wiring
// ─────────────────────────────────────────────────────────────────────
(function setupChat() {
    var input = document.getElementById("chat-input");
    var sendBtn = document.getElementById("chat-send-btn");
    var stopBtn = document.getElementById("chat-stop-btn");
    var newBtn = document.getElementById("chat-new-btn");
    if (!input || !sendBtn) return;

    function setBusy(busy) {
        input.disabled = busy;
        sendBtn.hidden = busy;
        stopBtn.hidden = !busy;
    }

    sendBtn.addEventListener("click", function () {
        var text = input.value.trim();
        if (!text || !window.ws || window.ws.readyState !== 1) return;
        window.ws.send(JSON.stringify({ type: "agent_send", text: text }));
        input.value = "";
        setBusy(true);
    });

    stopBtn.addEventListener("click", function () {
        if (window.ws && window.ws.readyState === 1) {
            window.ws.send(JSON.stringify({ type: "agent_stop" }));
        }
    });

    newBtn.addEventListener("click", function () {
        if (window.ws && window.ws.readyState === 1) {
            window.ws.send(JSON.stringify({ type: "agent_new_chat" }));
        }
    });

    input.addEventListener("keydown", function (e) {
        if ((e.ctrlKey || e.metaKey) && e.key === "Enter") {
            e.preventDefault();
            sendBtn.click();
        }
    });

    // Exposed so the message handler can re-enable input when agent_done
    // / agent_error / agent_aborted arrives.
    window.__setChatBusy = setBusy;
})();

// ─────────────────────────────────────────────────────────────────────
// Claude chat rendering helpers
// ─────────────────────────────────────────────────────────────────────
function addChatMessage(prefix, text, cls) {
    var container = document.getElementById("log-messages");
    if (!container) return;
    var div = document.createElement("div");
    div.className = "log-message " + (cls || "");
    var prefixSpan = document.createElement("span");
    prefixSpan.className = "msg-prefix";
    prefixSpan.textContent = "[" + prefix + "] ";
    var textSpan = document.createElement("span");
    textSpan.textContent = text;
    div.appendChild(prefixSpan);
    div.appendChild(textSpan);
    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
}

// Streaming Claude text: each agent_response_chunk appends to the
// currently-open Claude bubble, creating one if none is open.
// During streaming the text is rendered as plain text for speed; on
// finalize, the accumulated buffer is re-rendered as markdown
// (sanitised via DOMPurify to keep it safe to drop into the DOM).
var _claudeStreamEl = null;
var _claudeStreamBubble = null;
var _claudeStreamBuffer = "";

function appendClaudeStream(text) {
    var container = document.getElementById("log-messages");
    if (!container) return;
    if (_claudeStreamEl === null) {
        var div = document.createElement("div");
        div.className = "log-message msg-claude";
        var prefixSpan = document.createElement("span");
        prefixSpan.className = "msg-prefix";
        prefixSpan.textContent = "[claude] ";
        var textSpan = document.createElement("span");
        textSpan.className = "msg-text";
        div.appendChild(prefixSpan);
        div.appendChild(textSpan);
        container.appendChild(div);
        _claudeStreamEl = textSpan;
        _claudeStreamBubble = div;
        _claudeStreamBuffer = "";
    }
    _claudeStreamBuffer += text;
    _claudeStreamEl.textContent = _claudeStreamBuffer;
    container.scrollTop = container.scrollHeight;
}

// Render phase feedback (post_<phase>.md content) inline in the chat.
// Triggered by phase_complete; only renders for failures and for
// phases whose feedback has meaningful content (the endpoint 404s if
// the file isn't there, in which case we silently skip).
function surfacePhaseFeedback(phase, status) {
    if (!phase) return;
    // Only on failure — success messages don't need a body splat.
    if (status !== "failed") return;
    fetch("/api/feedback/" + encodeURIComponent(phase))
        .then(function (r) {
            if (!r.ok) return null;
            return r.json();
        })
        .then(function (data) {
            if (!data) return;
            var body = data.body_md || data.body || data.summary || "";
            if (!body) return;
            renderInlineMarkdown(
                "feedback",
                "Phase \"" + phase + "\" — feedback:",
                body,
                "msg-feedback"
            );
        })
        .catch(function () { /* ignore — feedback may not be ready */ });
}

// Render a markdown body inline in the log as a system-ish message.
// Used by surfacePhaseFeedback. Sanitises via DOMPurify, parses via
// marked, and injects via createContextualFragment (avoids innerHTML).
function renderInlineMarkdown(prefix, headline, body, cls) {
    var container = document.getElementById("log-messages");
    if (!container) return;
    var div = document.createElement("div");
    div.className = "log-message " + (cls || "");

    var prefixSpan = document.createElement("span");
    prefixSpan.className = "msg-prefix";
    prefixSpan.textContent = "[" + prefix + "] ";
    div.appendChild(prefixSpan);

    var textSpan = document.createElement("span");
    textSpan.className = "msg-text msg-text-markdown";
    if (headline) {
        var head = document.createElement("strong");
        head.textContent = headline;
        textSpan.appendChild(head);
        textSpan.appendChild(document.createElement("br"));
    }
    if (typeof marked !== "undefined" && typeof DOMPurify !== "undefined") {
        var html = DOMPurify.sanitize(marked.parse(body));
        var range = document.createRange();
        var frag = range.createContextualFragment(html);
        textSpan.appendChild(frag);
        if (typeof hljs !== "undefined") {
            textSpan.querySelectorAll("pre code").forEach(function (b) {
                try { hljs.highlightElement(b); } catch (e) { /* ignore */ }
            });
        }
    } else {
        textSpan.appendChild(document.createTextNode(body));
    }

    div.appendChild(textSpan);
    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
}

function finalizeClaudeStream() {
    if (_claudeStreamEl !== null && _claudeStreamBuffer
            && typeof marked !== "undefined" && typeof DOMPurify !== "undefined") {
        var html = DOMPurify.sanitize(marked.parse(_claudeStreamBuffer));
        // Use createContextualFragment to inject parsed HTML without
        // tripping the security hook that flags raw innerHTML.
        var range = document.createRange();
        range.selectNodeContents(_claudeStreamEl);
        range.deleteContents();
        var frag = range.createContextualFragment(html);
        _claudeStreamEl.appendChild(frag);
        _claudeStreamEl.classList.add("msg-text-markdown");
        // Highlight code blocks if hljs is loaded.
        if (typeof hljs !== "undefined") {
            _claudeStreamEl.querySelectorAll("pre code").forEach(function (block) {
                try { hljs.highlightElement(block); } catch (e) { /* ignore */ }
            });
        }
    }
    _claudeStreamEl = null;
    _claudeStreamBubble = null;
    _claudeStreamBuffer = "";
}

// Thinking indicator: a pulsing "[claude] thinking…" placeholder shown
// while a turn is in flight but Claude hasn't produced visible output yet.
var _thinkingEl = null;
function showThinking() {
    if (_thinkingEl !== null) return;
    var container = document.getElementById("log-messages");
    if (!container) return;
    var div = document.createElement("div");
    div.className = "log-message msg-claude msg-thinking";
    var prefixSpan = document.createElement("span");
    prefixSpan.className = "msg-prefix";
    prefixSpan.textContent = "[claude] ";
    var textSpan = document.createElement("span");
    textSpan.textContent = "thinking…";
    div.appendChild(prefixSpan);
    div.appendChild(textSpan);
    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
    _thinkingEl = div;
}
function clearThinking() {
    if (_thinkingEl !== null) {
        _thinkingEl.remove();
        _thinkingEl = null;
    }
}

function summarizeToolInput(input) {
    if (!input || typeof input !== "object") return "";
    if (input.file_path) return input.file_path;
    if (input.path) return input.path;
    if (input.command) return "`" + truncate(input.command, 80) + "`";
    if (input.pattern) return "/" + input.pattern + "/";
    var keys = Object.keys(input);
    return keys.length > 0 ? keys[0] + "=…" : "";
}

function truncate(s, n) {
    if (!s || s.length <= n) return s || "";
    return s.slice(0, n - 1) + "…";
}

// ─────────────────────────────────────────────────────────────────────
// Tool approval modal
// ─────────────────────────────────────────────────────────────────────
(function setupApprovalModal() {
    var overlay = document.getElementById("agent-approval-overlay");
    var toolEl = document.getElementById("agent-approval-tool");
    var targetEl = document.getElementById("agent-approval-target");
    var bodyEl = document.getElementById("agent-approval-body");
    var approveBtn = document.getElementById("agent-approval-approve");
    var denyBtn = document.getElementById("agent-approval-deny");
    if (!overlay) return;

    var currentRequestId = null;

    function show(request_id, tool, input) {
        currentRequestId = request_id;
        toolEl.textContent = tool;
        targetEl.textContent = summarizeToolInput(input);
        bodyEl.textContent = formatToolPreview(tool, input);
        overlay.hidden = false;
    }

    function hide() {
        overlay.hidden = true;
        currentRequestId = null;
    }

    function respond(approved) {
        if (!currentRequestId || !window.ws || window.ws.readyState !== 1) {
            hide();
            return;
        }
        window.ws.send(JSON.stringify({
            type: "agent_tool_response",
            request_id: currentRequestId,
            decision: {
                hookSpecificOutput: {
                    hookEventName: "PreToolUse",
                    permissionDecision: approved ? "allow" : "deny",
                    permissionDecisionReason: approved ? "approved by user" : "denied by user",
                },
            },
        }));
        hide();
    }

    approveBtn.addEventListener("click", function () { respond(true); });
    denyBtn.addEventListener("click", function () { respond(false); });

    window.__showApprovalModal = show;
})();

function formatToolPreview(tool, input) {
    if (!input) return "";
    if (tool === "Edit" && input.old_string && input.new_string) {
        return "- " + input.old_string.split("\n").join("\n- ") + "\n"
             + "+ " + input.new_string.split("\n").join("\n+ ");
    }
    if (tool === "Write" && input.content) {
        return truncate(input.content, 800);
    }
    if (tool === "Bash" && input.command) {
        return "$ " + input.command;
    }
    return JSON.stringify(input, null, 2);
}

// ─────────────────────────────────────────────────────────────────────
// Requirements tab: case-study switcher, file list, preview, load
// ─────────────────────────────────────────────────────────────────────
(function setupRequirements() {
    var tabBtn = document.getElementById("tab-requirements");
    if (!tabBtn) return;

    var studySelect = document.getElementById("case-study-select");
    var listEl = document.getElementById("requirements-list");
    var previewHeader = document.getElementById("requirements-preview-header");
    var previewCode = document.getElementById("requirements-preview-code");
    var selectAllBtn = document.getElementById("req-select-all-btn");
    var deselectBtn = document.getElementById("req-deselect-btn");
    var loadBtn = document.getElementById("req-load-btn");

    var state = {
        active: "",
        available: [],
        requirements: [],
        systemDescriptionPath: null,
        selected: new Set(),
    };

    function fetchCaseStudies() {
        return fetch("/api/case-studies").then(function (r) { return r.json(); });
    }

    function fetchRequirements(study) {
        var url = "/api/requirements" + (study ? "?study=" + encodeURIComponent(study) : "");
        return fetch(url).then(function (r) { return r.json(); });
    }

    function setActiveCaseStudy(study) {
        return fetch("/api/active-case-study", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ study: study }),
        }).then(function (r) { return r.json(); });
    }

    function renderStudySelect() {
        studySelect.replaceChildren();
        state.available.forEach(function (s) {
            var opt = document.createElement("option");
            opt.value = s;
            opt.textContent = s;
            if (s === state.active) opt.selected = true;
            studySelect.appendChild(opt);
        });
    }

    function renderList() {
        listEl.replaceChildren();

        // System description is a separate kind of context, surfaced at
        // the top with a visual marker. Checked by default; user can
        // uncheck to omit it from the loaded message.
        if (state.systemDescriptionPath) {
            listEl.appendChild(makeRow({
                path: state.systemDescriptionPath,
                name: "system_description.txt",
                size: null,
                kind: "system",
            }));
        }

        state.requirements.forEach(function (req) {
            listEl.appendChild(makeRow({
                path: req.path,
                name: req.name,
                size: req.size,
                kind: "requirement",
            }));
        });

        loadBtn.disabled = state.selected.size === 0;
    }

    function makeRow(item) {
        var row = document.createElement("div");
        row.className = "requirements-row";
        if (item.kind === "system") row.classList.add("system-row");
        if (state.selected.has(item.path)) row.classList.add("selected");

        var checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.checked = state.selected.has(item.path);
        checkbox.addEventListener("click", function (e) {
            e.stopPropagation();
            toggleSelect(item.path);
        });

        var nameSpan = document.createElement("span");
        nameSpan.className = "req-name";
        nameSpan.textContent = item.name;
        if (item.kind === "system") {
            var marker = document.createElement("span");
            marker.className = "req-system-marker";
            marker.textContent = " · system";
            nameSpan.appendChild(marker);
        }

        var sizeSpan = document.createElement("span");
        sizeSpan.className = "req-size";
        sizeSpan.textContent = item.size != null
            ? (item.size / 1024).toFixed(1) + " KB"
            : "";

        row.appendChild(checkbox);
        row.appendChild(nameSpan);
        row.appendChild(sizeSpan);
        row.addEventListener("click", function () {
            previewFile({ path: item.path, name: item.name });
        });
        return row;
    }

    function toggleSelect(path) {
        if (state.selected.has(path)) state.selected.delete(path);
        else state.selected.add(path);
        renderList();
    }

    function previewFile(req) {
        previewHeader.textContent = req.path;
        previewCode.textContent = "loading…";
        // req.path is relative to forge.assets/ (e.g.
        // "case-studies/lre/requirements/tier1_foundation.json").
        // /api/asset serves files under that root.
        fetch("/api/asset/" + req.path)
            .then(function (r) { return r.json(); })
            .then(function (data) {
                if (data.content !== undefined) {
                    previewCode.textContent = data.content;
                } else if (data.error) {
                    previewCode.textContent = "Error: " + data.error;
                }
            });
    }

    function loadRequirements() {
        if (state.selected.size === 0 || !ws || ws.readyState !== 1) return;
        var sysPath = state.systemDescriptionPath;
        var selectedPaths = Array.from(state.selected).sort();
        var reqPaths = selectedPaths.filter(function (p) { return p !== sysPath; });
        var sysSelected = sysPath && state.selected.has(sysPath);

        var lines = [];
        if (reqPaths.length > 0) {
            lines.push("Work from these requirement files in the active case study:");
            reqPaths.forEach(function (p) { lines.push("  - " + p); });
        }
        if (sysSelected) {
            if (lines.length > 0) lines.push("");
            lines.push("System description: " + sysPath);
            lines.push("Read it first as context.");
        }
        lines.push("");
        lines.push("Walk me through what needs to be implemented; do not start writing code yet.");
        ws.send(JSON.stringify({ type: "agent_send", text: lines.join("\n") }));
        if (window.__setChatBusy) window.__setChatBusy(true);
        if (typeof setTaskStatus === "function") setTaskStatus("requirements", "completed");
    }

    function refreshRequirementsList() {
        return fetchRequirements(state.active).then(function (data) {
            state.requirements = data.requirements || [];
            state.systemDescriptionPath = data.system_description_path || null;
            // System description is checked by default (always-relevant
            // context); user can uncheck to omit.
            state.selected = new Set();
            if (state.systemDescriptionPath) {
                state.selected.add(state.systemDescriptionPath);
            }
            renderList();
        });
    }

    selectAllBtn.addEventListener("click", function () {
        state.selected = new Set(state.requirements.map(function (r) { return r.path; }));
        if (state.systemDescriptionPath) {
            state.selected.add(state.systemDescriptionPath);
        }
        renderList();
    });
    deselectBtn.addEventListener("click", function () {
        state.selected = new Set();
        renderList();
    });
    loadBtn.addEventListener("click", loadRequirements);

    studySelect.addEventListener("change", function () {
        var picked = studySelect.value;
        setActiveCaseStudy(picked).then(function () {
            state.active = picked;
            return refreshRequirementsList();
        });
    });

    tabBtn.addEventListener("click", function () {
        if (state.available.length === 0) {
            fetchCaseStudies().then(function (data) {
                state.active = data.active || "";
                state.available = data.available || [];
                renderStudySelect();
                return refreshRequirementsList();
            });
        }
    });
})();

// ─────────────────────────────────────────────────────────────────────
// Prompts tab (Phase 2): vibe-coding layers (selectable) + always-loaded
// context prompts (.txt files in forge.assets/prompts/).
// ─────────────────────────────────────────────────────────────────────
(function setupPrompts() {
    var tabBtn = document.getElementById("tab-prompts");
    if (!tabBtn) return;

    var listEl = document.getElementById("prompts-list");
    var previewHeader = document.getElementById("prompts-preview-header");
    var previewCode = document.getElementById("prompts-preview-code");
    var selectAllBtn = document.getElementById("prompts-select-all-btn");
    var deselectBtn = document.getElementById("prompts-deselect-btn");
    var loadBtn = document.getElementById("prompts-load-btn");

    var state = {
        layers: [],
        contextPaths: [],
        selected: new Set(),
        loaded: false,
    };

    function fetchPrompts() {
        return fetch("/api/codegen-prompts").then(function (r) { return r.json(); });
    }

    function renderList() {
        listEl.replaceChildren();

        // Context (always-loaded) section header + rows — uncheckable
        // indicators that these are referenced as background context.
        if (state.contextPaths.length > 0) {
            var hdr = document.createElement("div");
            hdr.className = "requirements-row system-row";
            var hdrName = document.createElement("span");
            hdrName.className = "req-name";
            hdrName.textContent = "Always-loaded context (" + state.contextPaths.length + " files)";
            var marker = document.createElement("span");
            marker.className = "req-system-marker";
            marker.textContent = " · context";
            hdrName.appendChild(marker);
            hdr.appendChild(hdrName);
            listEl.appendChild(hdr);

            state.contextPaths.forEach(function (p) {
                var row = document.createElement("div");
                row.className = "requirements-row";
                row.style.paddingLeft = "32px";
                var nameSpan = document.createElement("span");
                nameSpan.className = "req-name";
                nameSpan.style.color = "var(--bone-dim)";
                nameSpan.textContent = p.split("/").pop();
                row.appendChild(nameSpan);
                row.addEventListener("click", function () {
                    previewFile({ path: p, name: p.split("/").pop() });
                });
                listEl.appendChild(row);
            });
        }

        // Selectable layer rows
        state.layers.forEach(function (layer) {
            var row = document.createElement("div");
            row.className = "requirements-row";
            if (state.selected.has(layer.path)) row.classList.add("selected");

            var checkbox = document.createElement("input");
            checkbox.type = "checkbox";
            checkbox.checked = state.selected.has(layer.path);
            checkbox.addEventListener("click", function (e) {
                e.stopPropagation();
                toggleSelect(layer.path);
            });

            var nameSpan = document.createElement("span");
            nameSpan.className = "req-name";
            nameSpan.textContent = layer.name;

            var sizeSpan = document.createElement("span");
            sizeSpan.className = "req-size";
            sizeSpan.textContent = (layer.size / 1024).toFixed(1) + " KB";

            row.appendChild(checkbox);
            row.appendChild(nameSpan);
            row.appendChild(sizeSpan);
            row.addEventListener("click", function () { previewFile(layer); });
            listEl.appendChild(row);
        });

        loadBtn.disabled = state.selected.size === 0;
    }

    function toggleSelect(path) {
        if (state.selected.has(path)) state.selected.delete(path);
        else state.selected.add(path);
        renderList();
    }

    function previewFile(item) {
        previewHeader.textContent = item.path;
        previewCode.textContent = "loading…";
        fetch("/api/asset/" + item.path)
            .then(function (r) { return r.json(); })
            .then(function (data) {
                if (data.content !== undefined) {
                    previewCode.textContent = data.content;
                } else if (data.error) {
                    previewCode.textContent = "Error: " + data.error;
                }
            });
    }

    function loadSelected() {
        if (state.selected.size === 0 || !ws || ws.readyState !== 1) return;
        var selectedPaths = Array.from(state.selected).sort();
        var lines = [];
        lines.push("Phase 2 — Java codegen. Run these prompt layers in order:");
        selectedPaths.forEach(function (p) { lines.push("  - " + p); });
        if (state.contextPaths.length > 0) {
            lines.push("");
            lines.push("Always-loaded context (read these for the rules and patterns):");
            state.contextPaths.forEach(function (p) { lines.push("  - " + p); });
        }
        lines.push("");
        lines.push("Read each layer file and execute it as the prompt; halt for review between layers.");
        ws.send(JSON.stringify({ type: "agent_send", text: lines.join("\n") }));
        if (window.__setChatBusy) window.__setChatBusy(true);
        if (typeof setTaskStatus === "function") setTaskStatus("code_synthesis", "completed");
    }

    selectAllBtn.addEventListener("click", function () {
        state.selected = new Set(state.layers.map(function (l) { return l.path; }));
        renderList();
    });
    deselectBtn.addEventListener("click", function () {
        state.selected = new Set();
        renderList();
    });
    loadBtn.addEventListener("click", loadSelected);

    tabBtn.addEventListener("click", function () {
        if (!state.loaded) {
            fetchPrompts().then(function (data) {
                state.layers = data.layers || [];
                state.contextPaths = data.context_paths || [];
                state.loaded = true;
                renderList();
            });
        }
    });
})();

// ─────────────────────────────────────────────────────────────────────
// Source tab (Phase 2): browse generated java.generated.project source.
// ─────────────────────────────────────────────────────────────────────
(function setupSource() {
    var tabBtn = document.getElementById("tab-source");
    if (!tabBtn) return;

    var listEl = document.getElementById("source-list");
    var previewHeader = document.getElementById("source-preview-header");
    var previewCode = document.getElementById("source-preview-code");
    var refreshBtn = document.getElementById("source-refresh-btn");
    var countEl = document.getElementById("source-file-count");

    var state = { files: [], loaded: false };

    function fetchSourceFiles() {
        return fetch("/api/source-files").then(function (r) { return r.json(); });
    }

    function renderList() {
        listEl.replaceChildren();
        countEl.textContent = state.files.length === 0
            ? "no java files"
            : state.files.length + " file" + (state.files.length === 1 ? "" : "s");

        if (state.files.length === 0) {
            var empty = document.createElement("div");
            empty.className = "requirements-row";
            empty.style.color = "var(--bone-faint)";
            empty.style.fontStyle = "italic";
            empty.textContent = "No .java files yet — run codegen first.";
            listEl.appendChild(empty);
            return;
        }

        state.files.forEach(function (f) {
            var row = document.createElement("div");
            row.className = "requirements-row";

            var nameSpan = document.createElement("span");
            nameSpan.className = "req-name";
            nameSpan.textContent = f.path;

            var sizeSpan = document.createElement("span");
            sizeSpan.className = "req-size";
            sizeSpan.textContent = (f.size / 1024).toFixed(1) + " KB";

            row.appendChild(nameSpan);
            row.appendChild(sizeSpan);
            row.addEventListener("click", function () { previewFile(f); });
            listEl.appendChild(row);
        });
    }

    function previewFile(f) {
        previewHeader.textContent = f.path;
        previewCode.textContent = "loading…";
        fetch("/api/source/" + f.path)
            .then(function (r) { return r.json(); })
            .then(function (data) {
                if (data.content !== undefined) {
                    previewCode.textContent = data.content;
                } else if (data.error) {
                    previewCode.textContent = "Error: " + data.error;
                }
            });
    }

    function refresh() {
        return fetchSourceFiles().then(function (data) {
            state.files = data.files || [];
            state.loaded = true;
            renderList();
        });
    }

    refreshBtn.addEventListener("click", refresh);
    tabBtn.addEventListener("click", function () {
        if (!state.loaded) refresh();
    });
})();


// Phase 7 — slash commands list. Exposes loadCommands() so the phase
// click handler can refresh on selection.
var loadCommands;
(function setupCommands() {
    var listEl = document.getElementById("commands-list");
    if (!listEl) return;
    var state = { loaded: false };

    function fillChatWithCommand(name) {
        var input = document.getElementById("chat-input");
        if (!input) return;
        input.value = "/" + name + " ";
        input.focus();
        // Move caret to end so the user can append args if any.
        try { input.setSelectionRange(input.value.length, input.value.length); }
        catch (e) { /* not all browsers support setSelectionRange */ }
    }

    function render(commands) {
        listEl.replaceChildren();
        if (!commands.length) {
            var empty = document.createElement("div");
            empty.className = "requirements-row";
            empty.textContent = "No slash commands found in .claude/commands/.";
            listEl.appendChild(empty);
            return;
        }
        commands.forEach(function (cmd) {
            var row = document.createElement("div");
            row.className = "requirements-row commands-row";
            row.title = "Click to insert /" + cmd.name + " into the chat";

            var name = document.createElement("span");
            name.className = "commands-name";
            name.textContent = "/" + cmd.name;

            var desc = document.createElement("span");
            desc.className = "commands-desc";
            desc.textContent = cmd.description || "";

            row.appendChild(name);
            row.appendChild(desc);
            row.addEventListener("click", function () {
                fillChatWithCommand(cmd.name);
            });
            listEl.appendChild(row);
        });
    }

    loadCommands = function () {
        return fetch("/api/commands")
            .then(function (r) { return r.json(); })
            .then(function (data) {
                state.loaded = true;
                render(data.commands || []);
            })
            .catch(function () {
                render([]);
            });
    };
})();
