"""Per-study configuration for the generalised Tier-B enumeration.

`events` is the enumeration universe, in the published order/spelling.
`ev_thy` maps each enumerated event to the Isabelle Evt constructor (None =
an event the controller has no InputEvent case for; the published LRE run used
the literal label 'tick' for exactly that role).
"""
from collections import OrderedDict

LE, GE = '\\<le>', '\\<ge>'

LRE_PREDS = ["camActive", "hcmActive", "inOpez", "momReturn", "velBelowOrAtOne",
             "odistCdynAboveOne", "odistCstcAboveOne", "cdaAboveOrAtMinSafe"]

STUDIES = OrderedDict()

STUDIES['lre'] = dict(
    thy='LreController_Beh.thy',
    modes=["OCM", "MOM", "HCM", "CAM"],
    gvars=OrderedDict((p, [False, True]) for p in LRE_PREDS),
    events=["ReqVel", "ReqHdng", "ReqMOM", "ReqOCM", "EndTask", "ReqHCM", "tick"],
    ev_thy={"ReqVel": "reqVel", "ReqHdng": "reqHdng", "ReqMOM": "reqMOM",
            "ReqOCM": "reqOCM", "EndTask": "endTask", "ReqHCM": "reqHCM",
            "tick": None},
    input_events={"reqVel", "reqHdng", "reqMOM", "reqOCM", "endTask", "reqHCM"},
    atoms={
        'vel' + LE + '1.0':        lambda g: g['velBelowOrAtOne'],
        'inOpez':                  lambda g: g['inOpez'],
        'odist(cdyn)>1.0':         lambda g: g['odistCdynAboveOne'],
        'odist(cstc)>1.0':         lambda g: g['odistCstcAboveOne'],
        'camActive':               lambda g: g['camActive'],
        'hcmActive':               lambda g: g['hcmActive'],
        'momReturn':               lambda g: g['momReturn'],
        'cda' + GE + 'minsafedist()': lambda g: g['cdaAboveOrAtMinSafe'],
    },
    java_kind='lre',
    java_rel='java/controller/LreController.java',
)

STUDIES['sranger'] = dict(
    thy='SRangerController_Beh.thy',
    modes=["Moving", "Turning", "Final"],
    gvars=OrderedDict([("obstacleDetected", [False, True]),
                       ("turnDurationElapsed", [False, True])]),
    events=["endTask", "move", "obstacle", "markReset", "tick"],
    ev_thy={e: e for e in ["endTask", "move", "obstacle", "markReset", "tick"]},
    input_events={"endTask", "obstacle", "tick"},
    atoms={
        'distance()' + LE + 'obstaclethreshold()': lambda g: g['obstacleDetected'],
        'turnDurationElapsed':                     lambda g: g['turnDurationElapsed'],
    },
    java_kind='sranger',
    java_rel='java/controller/SRangerController.java',
)

STUDIES['chemical_detector'] = dict(
    thy='GasAnalysisController_Beh.thy',
    modes=["Reading", "Analysis", "NoGas", "GasDetected", "Final"],
    gvars=OrderedDict([("stsIsGasD", [False, True]),
                       ("insAboveThr", [False, True])]),
    events=["gas", "tick", "resume", "stop", "turn"],
    ev_thy={e: e for e in ["gas", "tick", "resume", "stop", "turn"]},
    input_events={"gas", "tick"},
    atoms={
        'sts= (noGas)':      lambda g: not g['stsIsGasD'],
        'sts= (gasD)':       lambda g: g['stsIsGasD'],
        'insVal' + GE + '10.0': lambda g: g['insAboveThr'],
    },
    java_kind='chem',
    java_rel='java/gasanalysis/GasAnalysisController.java',
)
