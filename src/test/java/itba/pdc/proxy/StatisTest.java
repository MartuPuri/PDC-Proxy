package itba.pdc.proxy;

import itba.pdc.proxy.model.EHttpRequest;

public class StatisTest {
	public static void main(String[] args) {
		EHttpRequest e = new EHttpRequest();
		e.add("authorization", "value");
	}
}
