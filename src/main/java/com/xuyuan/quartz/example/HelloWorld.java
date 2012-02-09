package com.xuyuan.quartz.example;import java.text.ParseException;import java.text.SimpleDateFormat;import java.util.Date;import org.quartz.JobDetail;import org.quartz.Scheduler;import org.quartz.SchedulerException;import org.quartz.SimpleTrigger;import org.quartz.Trigger;import org.quartz.impl.StdSchedulerFactory;public class HelloWorld {	/**	 * Grab the Scheduler instance from the Factory	 * and start it off	 * Define job instance	 * Define a Trigger that will fire "now"	 * Schedule the job with the trigger	 * @param args	 * @throws SchedulerException 	 * @throws ParseException 	 */	public static void main(String[] args) throws SchedulerException, ParseException {		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();		scheduler.start();				JobDetail job = new JobDetail("job1", "group1", MyJobClass.class);				/**		 * SimpleTrigger构造函数: name,group,startTime,endTime,repeatCount,repeatInterval		 */		//立即执行,执行1次. 重复次数和重复间隔不设置.					Trigger trigger = new SimpleTrigger("trigger1", "group1", new Date());		//立即执行,执行1次. 重复次数和重复间隔手动设为0.							trigger = new SimpleTrigger("trigger1", "group1", new Date(),null,0,0L);		//在指定的某个时刻执行,执行1次		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");		Date runDate = df.parse("2011-12-30 14:06:00");		trigger = new SimpleTrigger("trigger1", "group1", runDate);				//repeatCount>0,repeatInterval不能为0:	org.quartz.SchedulerException: Repeat Interval cannot be zero.		//trigger = new SimpleTrigger("trigger1", "group1", new Date(),null,1,0L);		//立即执行,每隔1000ms[1s],执行2次. 		repeatCount=1表示执行了2次.有点怪怪的.		trigger = new SimpleTrigger("trigger1", "group1", new Date(),null,1,1000L);				scheduler.scheduleJob(job, trigger);		//也可以放在这里启动调度器		//scheduler.start();		//如果关闭调度器,则无法打印MyJobClass中的日志		//scheduler.shutdown();	}}