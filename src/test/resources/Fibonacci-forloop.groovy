package refreshable.service.impl

import refreshable.service.FibonacciService;

/**
 * Example Groovy class.
 */
public class Fibonacci implements FibonacciService {
	
	public Fibonacci() {
	}
	
	public void execute(String sequence) {
		println "Using For loop"
		Integer number = Integer.valueOf(sequence);
		int result = 0;
		for (int i = 1; i <= number; i++) {
			result += i;
		}
		println result;
	}
}
