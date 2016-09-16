package scripts.api.patterns;

public abstract class Node {

    public BaseScript script;

    public Node(BaseScript script) { this.script = script; }

    public abstract void execute();

    public abstract boolean validate();
	
}