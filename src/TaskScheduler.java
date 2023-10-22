
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ConcurrentModificationException;
import java.util.List;

public class TaskScheduler extends Thread {
	
	public static enum SchedulingAlgorithm {
		FIFO, Priority;
	}

	private CopyOnWriteArrayList<Task> tasksRunning = new CopyOnWriteArrayList<Task>();
	private List<Task> tasksReady = Collections.synchronizedList(new ArrayList<Task>());
	
	private SchedulingAlgorithm schedulingAlgorithm = SchedulingAlgorithm.FIFO;
	
	private int numThreads = 0; 
	private int maxNumThreads = 1;
	
	public class AutoScheduler extends Thread {
		
		public AutoScheduler() {
			super();
		}
		
		@Override
		public void run() {
			ReentrantLock lock = new ReentrantLock();
			while(true) {
				lock.lock();
				if(numThreads < maxNumThreads) {
					if(tasksReady.size() > 0) {
						Task nextTask;
						switch(schedulingAlgorithm) {
						case FIFO:
							nextTask = tasksReady.get(0);
							runTask(nextTask);
							break;
						case Priority:
							nextTask = Collections.min(tasksReady, Comparator.comparing(t -> t.priority));
							runTask(nextTask);
							break;
						}
					}
				}
				lock.unlock();
			}
		}
		
		private void runTask(Task task) {
			switch(task.state) {
			case Paused:
				resumeTask(task);
				break;
			case Ready:
				startTask(task);
				break;
			default:
				return;			
			}
		}
	}
	
	
	
	public TaskScheduler() {
		super();
	}
	
	public TaskScheduler(int maxNumThreads) {
		this.maxNumThreads = maxNumThreads;
	}
	
	public TaskScheduler(SchedulingAlgorithm schedAlg) {
		schedulingAlgorithm = schedAlg;
	}
	
	public TaskScheduler(int maxNumThreads, SchedulingAlgorithm schedAlg) {
		this.maxNumThreads = maxNumThreads;
		schedulingAlgorithm = schedAlg;
	}
	
	
	public List<Task> getTasksRunning(){
		return tasksRunning;
	}
	
	private boolean taskEnqueued(int taskId) {
		for(Task task : tasksReady) {
			if(task.taskId == taskId)
				return true;
		}
		return false;
	}
	
	protected boolean enqueueTask(Task task) {
		ReentrantLock lock = new ReentrantLock();
		lock.lock();
		if(taskEnqueued(task.taskId)) {
			lock.unlock();
			return false;
		}
		if(task.state == Task.TaskState.Paused) {
			if(tasksReady.size() == 0)
				tasksReady.add(task);
			else {
				for(int i=0; i<tasksReady.size(); i++) {
					if(tasksReady.get(i).state != Task.TaskState.Paused) {
						tasksReady.add(i, task);
						break;
					}
				}
			}	
		}
		else
		{
			task.state = Task.TaskState.Ready;
			tasksReady.add(task);
		}
		lock.unlock();
		return true;
	}
	
	public void dequeueTask(Task task) {
		tasksReady.remove(task);
	}
	
	private Task lowestPriorityRunningTask() throws Exception {
		if(tasksRunning.size() == 0)
			throw new Exception();
		int maxPriority = 0;
		Task taskToPause = new Task();
		for(Task taskIt : tasksRunning) {
			if(taskIt.priority > maxPriority) {
				maxPriority = taskIt.priority;
				taskToPause = taskIt;
			}
		}
		return taskToPause;
	}
	
	public boolean startTask(Task task) {
		switch(task.state) {
		case Running:
		case Paused:  
			System.out.println("Cannot start Task " + task.taskId + " - already started.");
			return false;
		case Finished:
			finishTask(task);
			return false;
		case NotEnqueued:
			enqueueTask(task);
		case Ready:
			if(numThreads < maxNumThreads)
			{
				dequeueTask(task);
				
				tasksRunning.add(task);
				task.start();
				task.state = Task.TaskState.Running;
				numThreads++;
				System.out.println(task + " STARTED.");
				return true;
			}
			else {
				try {
					Task taskToPause = lowestPriorityRunningTask();
					
					pauseTask(taskToPause);
					if(startTask(task));
						return true;
				}
				catch(Exception ex) {
					//ex.printStackTrace();
					return false;
				}
			}
		default:
			return false;
		}
	}
	
	public boolean pauseTask(Task task) {
		if(tasksRunning.contains(task)) {
			switch(task.state) {
			case Paused:
				System.out.println("Cannot pause Task " + task.taskId + " - already paused.");
				return false;
			case Finished:
				finishTask(task);
				return false;
			case NotEnqueued:
			case Ready:
				System.out.println("Cannot pause Task " + task.taskId + " - task not started.");
				return false;
			case Running:
				break;
			default:
				return false;
			}	
			
			try {
				synchronized (task){
					task.suspend();
				}	
			} catch(Exception ex) {
				return false;
			}
			tasksRunning.remove(task);
			task.state = Task.TaskState.Paused;
			enqueueTask(task);
			numThreads--;
			return true;
		}
		return false;
	}
	
	public boolean resumeTask(Task task) {
		if(tasksReady.contains(task)) {
			switch(task.state) {
			case Paused:
				if(numThreads < maxNumThreads) {
					task.resume();
					tasksRunning.add(task);
					dequeueTask(task);
					task.state = Task.TaskState.Running;
					numThreads++;
					return true;
				}
				else {
					try {
						Task taskToPause = lowestPriorityRunningTask();
						
						pauseTask(taskToPause);
						if(resumeTask(task));
							return true;
					}
					catch(Exception ex) {
						//ex.printStackTrace();
						return false;
					}
					
				}
			case Finished:
				finishTask(task);
				return false;
			case Ready:
			case Running:
			case NotEnqueued:
				System.out.println("Cannot resume Task " + task.taskId + " - task not paused.");
			default:
				return false;
			}
		}
		return false;
	}
	
	public boolean finishTask(Task task) {
		if(tasksRunning.contains(task)) {
			tasksRunning.remove(task);
			numThreads--;
			System.out.println(task + " IS FINISHED.");
			return true;
		}
		return false;
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				synchronized(tasksRunning) {
					if(tasksRunning.size() == 0)
						continue;
					for(Task task : tasksRunning) {
						if(task.state == Task.TaskState.Finished)
							finishTask(task);	
					}
				}
				
			} catch(ConcurrentModificationException ignored) {
				continue;
			}
			
		}
	}
}
