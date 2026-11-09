package syn20.event;

public sealed interface InputEvent {
    record Advance() implements InputEvent {}
    record Retreat() implements InputEvent {}
    record Tick() implements InputEvent {}
    record EndTask() implements InputEvent {}
}
