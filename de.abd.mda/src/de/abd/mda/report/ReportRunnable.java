package de.abd.mda.report;

public class ReportRunnable implements Runnable {

	@Override
	public void run() {
		System.out.println("Hallo Thread!!!");
		for (int i = 0; i < 1000; i++) {
			System.out.println("Jetzt " + i);
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
