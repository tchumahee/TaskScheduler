
public class TaskA extends Task {
	
	public TaskA() {
		super();
	}
	
	public TaskA(int priority) {
		super(priority);
	}
	
	@Override
	protected final void taskAction() {
		for(int i = 0; i<100; i++)
			System.out.println("Task " + this.taskId + " IS RUNNING.");
	}
}
