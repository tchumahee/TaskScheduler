
public class Task extends Thread {
	protected static enum TaskState {
		NotEnqueued, Ready, Running, Paused, Finished;
	}
	
	private static int numTasks = 0;
	
	protected int taskId;
	protected TaskState state = TaskState.NotEnqueued;
	protected int priority = 7;
	
	public Task(int priority) {
		this();
		this.priority = priority;
	}
	
	public Task() {
		this.taskId = ++numTasks;	
	}
	
	@Override
	public final void run() {
		taskAction();
		state = TaskState.Finished;
	}
	
	protected void taskAction() {
	}
	
	public int getTaskId() {
		return taskId;
	}
	
	@Override
	public String toString() {
		return ("Task " + this.taskId);
	}
}
