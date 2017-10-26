import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Lab 1 — Linker
 * @author Ashish Ramachandran (ar3986)
 *
 */

public class Lab1 {

	public static ArrayList<String> lines = new ArrayList<String>();
	public static Object[][][] contents;
	public static final int DEF_INDEX = 0, USE_INDEX = 1, TEXT_INDEX = 2;
	public static ArrayList<Symbol> declaredSymbols = new ArrayList<Symbol>();
	public static int[] moduleSizes;
	public static int machineSize = 600;
	public static String symbolTableErrors = "";
	public static String memoryMapErrors = "";

	public static void main(String[] args) {
		populateLinesArray(args[0]);
		createContents();

		createSymbolTablePass1();
		createMemoryMapPass2();
	}

	private static void populateLinesArray(String input) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(input));
			while(true) {
				String line = in.readLine();
				if(line == null)
					break;
				if(line.isEmpty())
					continue;
				lines.add(line.trim());
			}
			in.close();
		} catch (Exception e) {

		}
	}

	private static void createSymbolTablePass1() {
		System.out.println("Symbol Table");
		//create absolute addresses
		for(Symbol symbol : declaredSymbols) {
			int totalDistance = 0;
			for(int i = symbol.module - 1; i >= 0; i--) {
				totalDistance += moduleSizes[i];
			}
			if(symbol.relativeAdd > moduleSizes[symbol.module]) {
				symbol.relativeAdd = 0;
				symbolTableErrors += "Error: The address appearing the definition symbol for \"" + symbol.symbol + "\" exceeds size of the module. Address treated as 0 (relative)."; 
			}
			symbol.absoluteAdd = symbol.relativeAdd + totalDistance;
			System.out.println(symbol.symbol + "=" + symbol.absoluteAdd);
		}
		System.out.println("\n" + symbolTableErrors + "\n");
	}

	private static void createMemoryMapPass2() {
		System.out.println("Memory Map");
		int memoryLine = 0;
		//for each module
		for(int module = 0; module < contents.length; module++) {

			//check the uses
			for(int use = 0; use < contents[module][USE_INDEX].length; use++) {
				Symbol symbol = Symbol.getSymbolFromString((String) contents[module][USE_INDEX][use]);
				if(symbol != null) {
					symbol.numUsed++;
					symbol.inUseList = true;
				}
			}

			//for each text
			for(int textIterator = 0; textIterator < contents[module][TEXT_INDEX].length; textIterator++) {
				int text = (int) contents[module][TEXT_INDEX][textIterator];
				int type = text % 10;
				String errorMessage = "";
				int address = text / 10;

				if(type == 1) { //immediate
					System.out.println(memoryLine + ":\t" + address + " " + errorMessage);
				} else if(type == 2) { //absolute
					int temp = address % 1000;
					//if address is larger than the machine
					if(temp > machineSize) {
						errorMessage = "Error: Absolute address exceeds machine size; zero used.";
						address /= 1000;
						address *= 1000;
					}
					System.out.println(memoryLine + ":\t" + address + " " + errorMessage);
				} else if(type == 3) { //relative
					//System.out.println("type 3, " + module +  ", " + text);
					int relativeAddress = address % 1000;

					//check if relativeAddress is larger than the module size
					if(relativeAddress > moduleSizes[module]) {
						errorMessage = "Error: Relative address exceeds module size; zero used.";
						address /= 1000;
						address *= 1000;
						System.out.println(memoryLine + ":\t" + address + " " + errorMessage);
					} else {
						int totalDistance = 0;
						for(int i = module - 1; i >= 0; i--) {
							totalDistance += moduleSizes[i];
						}
						address += totalDistance;
						System.out.println(memoryLine + ":\t" + address + " " + errorMessage);
					}

				} else if(type == 4) { //external
					int useIndex = address % 1000;
					//external address is too large to reference an entry in the use list
					if(useIndex + 1 > contents[module][USE_INDEX].length) {
						errorMessage = "Error: External address exceeds length of use list; treated as immediate.";
						System.out.println(memoryLine + ":\t" + address + " " + errorMessage);
					} else {
						Symbol usedSymbol = Symbol.getSymbolFromString((String) contents[module][USE_INDEX][useIndex]);
						if(usedSymbol != null) {
							address /= 1000;
							address *= 1000;
							address += usedSymbol.absoluteAdd;
							System.out.println(memoryLine + ":\t" + address + " " + errorMessage);
							Symbol.getSymbolFromList(usedSymbol).usedInModule = true;
						} else {
							errorMessage = "Error: \"" + contents[module][USE_INDEX][useIndex] + "\" is not defined; zero used.";
							address /= 1000;
							address *= 1000;
							System.out.println(memoryLine + ":\t" + address + " " + errorMessage);
						}
					}
				}
				memoryLine++;
			}

			//checks if symbol was in use list but not used in module
			for(int use = 0; use < contents[module][USE_INDEX].length; use++) {
				Symbol symbol = Symbol.getSymbolFromString((String) contents[module][USE_INDEX][use]);
				if(symbol != null) {
					if(symbol.inUseList && !symbol.usedInModule) {
						memoryMapErrors += "Warning: In module " + module + ", \"" + symbol.symbol + "\" is on use list but isn't used\n";
					}
					symbol.inUseList = false;
					symbol.usedInModule = false;
				}
			}
		}

		for(Symbol symbol : declaredSymbols) {
			if(symbol.numUsed == 0) {
				memoryMapErrors += "Warning: \"" + symbol.symbol + "\" was defined in module " + symbol.module + " but never used.\n";
			}
		}

		System.out.println("\n" + memoryMapErrors);
	}

	private static void createContents() {
		String allTogether = "";
		for(int i = 0; i < lines.size(); i++) {
			allTogether += lines.get(i) + (i == lines.size() - 1 ? "" : " ");
		}
		allTogether = allTogether.replaceAll(" +", " ");

		String[] splitLine = allTogether.split(" ");

		String[] newSplit = new String[splitLine.length - 1];
		for(int i = 1; i < splitLine.length; i++) {
			newSplit[i - 1] = splitLine[i];
		}

		int numModules = Integer.parseInt(splitLine[0]);

		moduleSizes = new int[numModules];
		contents = new Object[numModules][3][];
		int tracker = 0;
		int listType = 0;
		int module = 0;
		while(tracker < newSplit.length) {
			int length = Integer.parseInt(newSplit[tracker]);
			if(length >= 1) {
				if(listType == 0) {
					contents[module][listType] = new Object[length];
					for(int i = 0; i < length * 2; i += 2) {
						Symbol newAdd = new Symbol(newSplit[tracker + 1 + i], Integer.parseInt(newSplit[tracker + 1 + i + 1]), 0, 0, module, false, false);
						contents[module][listType][i/2] = newAdd;
						if(alreadyDeclaredSymbol(newAdd)) {
							symbolTableErrors += "Error: Symbol " + (newSplit[tracker + 1 + i]) + " is multiply defined. Using the value given in the first definition.\n";
						} else {
							declaredSymbols.add(newAdd);
						}
					}
				} else {
					contents[module][listType] = new Object[length];
					for(int i = 0; i < length; i++) {
						contents[module][listType][i] = (listType == 2 ? Integer.parseInt(newSplit[tracker + 1 + i]) : newSplit[tracker + 1 + i]);
					}
				}
			} else {
				contents[module][listType] = new Object[length];
			}

			if(listType == 0) {
				tracker += length * 2 + 1;
			} else {
				tracker += length + 1;
			}

			if(listType == 2) {
				listType = 0;
				module++;
			} else {
				listType++;
			}
		}

		for(int i = 0; i < moduleSizes.length; i++) {
			moduleSizes[i] = contents[i][2].length;
		}


	}

	public static boolean alreadyDeclaredSymbol(Symbol symbol) {
		for(Symbol symbolInList : declaredSymbols) {
			if(symbol.equals(symbolInList)) {
				return true;
			}
		}
		return false;
	}

	private static class Symbol {
		String symbol;
		int relativeAdd, absoluteAdd, numUsed, module;
		boolean inUseList, usedInModule;
		public Symbol(String symbol, int relativeAdd, int absoluteAdd, int numUsed, int module, boolean inUseList, boolean usedInModule) {
			this.symbol = symbol;
			this.relativeAdd = relativeAdd;
			this.absoluteAdd = absoluteAdd;
			this.numUsed = numUsed;
			this.module = module;
			this.inUseList = inUseList;
			this.usedInModule = usedInModule;
		}

		@Override
		public boolean equals(Object obj) {
			return this.symbol.equalsIgnoreCase(((Symbol) obj).symbol);
		}

		public static Symbol getSymbolFromString(String str) {
			for(Symbol symbol : declaredSymbols) {
				if(symbol.symbol.equalsIgnoreCase(str)) {
					return symbol;
				}
			}
			return null;
		}

		public static Symbol getSymbolFromList(Symbol symbol) {
			for(Symbol symbolInList : declaredSymbols) {
				if(symbolInList.equals(symbol)) {
					return symbolInList;
				}
			}
			return null;
		}
	}
}
