package refreshable.service.impl

import refreshable.service.FibonacciService;

/**
 * Example Groovy class.
 */
public class Fibonacci implements FibonacciService {
	
	public Fibonacci() {
	}
	
	public void execute(String sequence) {
		println "Using Recursive"
		Integer number = Integer.valueOf(sequence);
		int result = generateRecursive(number);
		println result;
	}
	private int generateRecursive(int sequence) {
		if ( sequence == 1) {
			return 1;
		} else {
			return sequence + generateRecursive(sequence -1);
		}
	}
}
