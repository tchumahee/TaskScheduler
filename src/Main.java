
public class Main {

	public static void main(String[] args) {
		TaskScheduler taskScheduler = new TaskScheduler(2, TaskScheduler.SchedulingAlgorithm.FIFO);
		taskScheduler.start();
		
		TaskA task1 = new TaskA();
		TaskA task2 = new TaskA(5);
		TaskA task3 = new TaskA(4);
		
		if(taskScheduler.enqueueTask(task1))
			System.out.println(task1 + " enqueued.");
		if(taskScheduler.enqueueTask(task2))
			System.out.println(task2 + " enqueued.");
		if(taskScheduler.enqueueTask(task3))
			System.out.println(task3 + " enqueued.");
		
		TaskScheduler.AutoScheduler autoScheduler = taskScheduler.new AutoScheduler();
		
		autoScheduler.start();
		
//		taskScheduler.startTask(task1);
//		taskScheduler.startTask(task2);
//		taskScheduler.startTask(task3);
//		
//		System.out.println("----- " + task1 + " state: " + task1.state + " -----");
//		System.out.println("----- " + task2 + " state: " + task2.state + " -----");
//		System.out.println("----- " + task3 + " state: " + task3.state + " -----");
//		
//		if(task1.state == Task.TaskState.Paused)
//			taskScheduler.resumeTask(task1);
//		if(task2.state == Task.TaskState.Paused)
//			taskScheduler.resumeTask(task2);
//		
//		System.out.println("----- " + task1 + " state: " + task1.state + " -----");
//		System.out.println("----- " + task2 + " state: " + task2.state + " -----");
//		System.out.println("----- " + task3 + " state: " + task3.state + " -----");
		
	}
}
