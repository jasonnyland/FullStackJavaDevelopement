package filehandling;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import comparators.FileNameComparatorAsc;
import driver.FileHandlingDisplay;
import exceptions.*;

public class FileHandler implements FileHandlerInterface {
	/*
	 * Number of choices from the main display and sub-display exclude option to
	 * exit the display
	 */
	public static final int MAIN_DISPLAY_CHOICES = 4;
	public static final int SUB_DISPLAY_CHOICES = 1;
	// REGEXs used for validating file names
	final String REGEX_FILENAME = "[A-Za-z0-9\\s\\.]*";
	final String REGEX_EXTENSION = ".*\\.txt$";
	/*
	 * checkTextFileExtension is a flag used in inputFileName() method. If true, use
	 * REGEX_EXTENSION to check if filename ends with ".txt". Otherwise, use
	 * REGEX_FILENAME to validate input filename
	 */
	boolean checkTextFileExtension = false;

	private File[] fileList;
	private String directoryPathString;

	public FileHandler() {
		super();
	}

	public FileHandler(File[] fileList) {
		super();
		this.fileList = fileList;
	}

	public FileHandler(String directoryPathString) {
		this.directoryPathString = directoryPathString;
		// Creating a File object for directory
		File directoryPath = new File(directoryPathString);

		// List of all files and directories
		this.fileList = directoryPath.listFiles();

	}

	public File[] getFileList() {
		return fileList;
	}

	public void setFileList(File[] fileList) {
		this.fileList = fileList;
	}

	@Override
	public void viewFiles() {
		/*
		 * This method lists all files and directory in fireList in ascending order
		 */
		if (fileList == null)
			System.out.println("This directory is emplty.");
		else {
			// Sort and print fileList in ascending order
			checkTextFileExtension = false;
			FileHandlingDisplay displayOptions = new FileHandlingDisplay();
			// Show view display
			displayOptions.viewDisplay();
			
			// Input user's choice
			int viewChoice;
			viewChoice = chooseAction(1);

			switch (viewChoice) {
			case 0:
				Arrays.sort(fileList, new FileNameComparatorAsc());
				System.out.println("");
				for (File file : fileList) {
					System.out.println(file.getName());
				}
				System.out.println("");
				break;
			case 1:
				List<File> allFiles = new ArrayList<>();
				dumpDirectory(directoryPathString, allFiles);
				Collections.sort(allFiles, new FileNameComparatorAsc());
				System.out.println("");
				for (File file : allFiles) {
					System.out.println(file.getName());
				}
				System.out.println("");
				break;
			}

		}
	}

	@Override
	public void addFile() {
		/*
		 * This method takes and validate a fileName from user. If fileName is invalid,
		 * creates and adds a new file with fileName to the current directory.
		 * Otherwise, declines creating a new file.
		 * 
		 * User is allowed to add a new text file only. For example, example.txt is a
		 * valid fileName.
		 */
		FileHandlingDisplay displayOptions = new FileHandlingDisplay();
		String fileName;
		int isContinued; // Equals to 0 if user chose to exit sub-display
		checkTextFileExtension = true; // Turn on checkTextFileExtension flag
		do {
			// Show Add display
			displayOptions.addDisplay();

			// Take confirmation from user to continue operating
			do {
				isContinued = chooseAction(SUB_DISPLAY_CHOICES);
			} while (isContinued == -1);

			System.out.println("You can only add text files (Ex: example.txt).");
			// Input fileName
			if (isContinued != 0) {
				do {
					fileName = inputFileName();
					/*
					 * In case user input invalid fileName, ask if user want to cancel operation
					 */
					if (fileName == null) {
						System.out.println("Try again - enter 1\nCancel - enter 0");
						do {
							isContinued = chooseAction(1);
						} while (isContinued == -1);
						if (isContinued == 0) {
							break;
						}
					}
				} while (fileName == null);

				// User cancels adding process
				if (fileName == null) {
					break;
				} else {
					// Check if fileName already exists
					refreshFileList();
					String existingFile = _searchExactFile(fileName, fileList);
					if (existingFile != null) {
						System.out.println(fileName + " already exists.");
					} else {
						// Add fileName to the directory
						File directory = new File(directoryPathString);
						File newFile = new File(directory, fileName);
						if (!newFile.exists()) {
							try {
								newFile.createNewFile();
							} catch (IOException e) {
								// e.printStackTrace();
								System.out.println("There is something wrong happened. Please try again later.\n");
							}
						}
						System.out.println(fileName + " is added.\n");
					}
				}
			}
		} while (isContinued != 0);
	}

	@Override
	public void searchFile() {
		/*
		 * This method searches for a file or directory in the directory by name and
		 * display name, absolute path, and size of the result(s) as well as number of
		 * results found.
		 */
		if (fileList == null) {
			System.out.println("This directory is empty");
			return;
		}
		checkTextFileExtension = false;
		FileHandlingDisplay displayOptions = new FileHandlingDisplay();
		String fileName;

		int isContinued; // Equals to 0 if user chose to exit sub-display
		do {
			// Show Search display
			displayOptions.searchDisplay();
			// Take confirmation from user to continue operating
			do {
				isContinued = chooseAction(SUB_DISPLAY_CHOICES);
			} while (isContinued == -1);

			// Input fileName
			if (isContinued != 0) {
				do {
					fileName = inputFileName();
					/*
					 * In case user input invalid fileName, ask if user want to cancel operation
					 */
					if (fileName == null) {
						System.out.println("Try again - enter 1\nCancel - enter 0");
						do {
							isContinued = chooseAction(1);
						} while (isContinued == -1);
						if (isContinued == 0) {
							break;
						}
					}
				} while (fileName == null);

				// User cancels operation
				if (fileName == null) {
					break;
				} else {
					// Search for fileName in the directory;
					int countFind = 0;
					countFind = _searchFile(fileName, fileList, countFind);

					System.out.println("-----");
					System.out.println("Searching completed. " + countFind + " found.");
				}
			}
		} while (isContinued != 0);

	}

	@Override
	public void deleteFile() {
		if (fileList == null) {
			System.out.println("This directory is empty");
			return;
		}
		checkTextFileExtension = false;
		FileHandlingDisplay displayOptions = new FileHandlingDisplay();
		String fileName;
		boolean isDeleted = false;

		int isContinued; // Equals to 0 if user chose to exit sub-display
		do {
			// Show Delete display
			displayOptions.deleteDisplay();
			// Take confirmation from user to continue operating
			do {
				isContinued = chooseAction(SUB_DISPLAY_CHOICES);
			} while (isContinued == -1);

			// Input fileName
			if (isContinued != 0) {
				do {
					fileName = inputFileName();
					/*
					 * In case user input invalid fileName, ask if user want to cancel operation
					 */
					if (fileName == null) {
						System.out.println("Try again - enter 1\nCancel - enter 0");
						do {
							isContinued = chooseAction(1);
						} while (isContinued == -1);
						if (isContinued == 0) {
							break;
						}
					}
				} while (fileName == null);

				// User cancels operation
				if (fileName == null) {
					break;
				} else {
					// Delete fileName from the directory;
					String fileNameToDelete = _searchExactFile(fileName, fileList);
					if (fileNameToDelete == null) {
						// fileName does not exist
						System.out.println(fileName + " does not exist");
					} else {
						try {
							File fileToDelete = new File(fileNameToDelete);
							isDeleted = _deleteFile(fileToDelete);
							if (isDeleted) {
								System.out.println(fileName + " is deleted");
								// Refresh fileList
								refreshFileList();
							} else {
								System.out.println("Unsuccessfuly delete" + fileName);
							}

						} catch (Exception e) {
							// e.printStackTrace();
							System.out.println("There is something wrong happened. Please try again later.");
						}
					}
				}
			}
		} while (isContinued != 0);

	}

	@Override
	public void updateFile() {
		// TODO Auto-generated method stub

	}

	public void refreshFileList() {
		/*
		 * This method refreshes the fileList
		 */
		fileList = new File(directoryPathString).listFiles();
	}

	public void dumpDirectory(String directoryName, List<File> files) {
		/*
		 * This method dumps all files in directoryName and its sub-directories into
		 * list files
		 */
		File directory = new File(directoryName);
		File[] fileList = directory.listFiles();

		if (fileList != null)
			for (File file : fileList) {
				if (file.isFile()) {
					files.add(file);
				} else if (file.isDirectory()) {
					dumpDirectory(file.getAbsolutePath(), files);
				}
			}
	}

	public int _searchFile(String fileName, File[] fileList, int countFind) {
		/*
		 * This method is a wrapper function for searchFile()
		 */

		for (File file : fileList) {
			if (file.getName().matches(fileName) || file.getName().matches(fileName + ".*")) {
				System.out.println("-----");
				System.out.println("Name: " + file.getName());
				System.out.println("Path: " + file.getAbsolutePath());
				System.out.println("Size: " + file.length());
				countFind++;
			}
			if (file.isDirectory()) {
				File subFileList[] = file.listFiles();
				_searchFile(fileName, subFileList, countFind);
			}
		}
		return countFind;
	}

	public String _searchExactFile(String fileName, File[] fileList) {
		/*
		 * This method is a wrapper function for deleteFile(). Returns absolute path of
		 * fileName if found. Otherwise, returns null.
		 */
		for (File file : fileList) {
			if (file.getName().matches(fileName)) {
				return file.getAbsolutePath();
			}
		}
		return null;
	}

	public static boolean _deleteFile(File fileToDelete) {
		// This method is a wrapper function for deleteFile
		File[] files = fileToDelete.listFiles();
		// If
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory())
					_deleteFile(file);
				else
					file.delete();
			}
		}
		return fileToDelete.delete();
	}

	public String inputFileName() {
		/*
		 * This method takes a fileName input by user and validates the input. A valid
		 * fileName must not be null and not longer than 128 characters. A valid
		 * fileName only contains alphabet characters, numbers, hyphen(-), underscore
		 * (_), and dot (.) The method returns null if fileName is invalid and returns
		 * fileName otherwise.
		 * 
		 * If checkTextFileExtension flag is true, use REGEX_EXTENSION to check if
		 * filename ends with ".txt". Otherwise, use REGEX_FILENAME to validate
		 * filename.
		 */
		Pattern pattern;
		if (checkTextFileExtension) {
			pattern = Pattern.compile(REGEX_EXTENSION);
		} else {
			pattern = Pattern.compile(REGEX_FILENAME);
		}
		String fileName = null;
		Scanner scan = new Scanner(System.in);
		try {
			System.out.print("Enter file/directory name: ");
			fileName = scan.next();
			if (fileName.isEmpty())
				throw new FileNameIsNullException("filename is null");
			else if (fileName.length() > 128)
				throw new FileNameIsTooLongException("filename is too long");
			else {
				if (!pattern.matcher(fileName).matches())
					throw new FileNameInvalidException("invalid filename");
			}
		} catch (FileNameIsNullException | FileNameIsTooLongException | FileNameInvalidException e) {
			if (!checkTextFileExtension) {
				System.out.println("Invalid input.\n" + "  - Filename cannot be empty.\n"
						+ "  - Filename must be no longer than 128 characters.\n"
						+ "  - Filename must not contain special characters excep .-_\n" + "Please try again!\n");
			} else {
				System.out.println(fileName + " is not a valid name for a text file.\nPlease try again!\n");
			}
			return null;
		}

		return fileName;
	}

	public int chooseAction(final int MAX_CHOICE) {
		/*
		 * This method takes and returns input from user for user's action choice from
		 * the main display
		 */
		Scanner scan = new Scanner(System.in);
		int choice = -1;
		try {
			System.out.print("Enter your choice: ");
			choice = scan.nextInt();
			if (choice < 0 || choice > MAX_CHOICE) {
				throw new UserChoiceIsInvalidException("invalid input");
			}
		} catch (UserChoiceIsInvalidException e) {
			System.out.println("Invalid input. Please try again!\n");
			return -1;
		} catch (Exception e) {
			System.out.println("Invalid input. Please try again!\n");
			return -1;
		}

		return choice;
	}

	public void performAction(int choice) {
		/*
		 * This method performs action chosen by user from main display
		 */
		switch (choice) {
		case 1:
			viewFiles();
			break;
		case 2:
			addFile();
			checkTextFileExtension = false; // Turn off checkTextFileExtension flag
			break;
		case 3:
			deleteFile();
			break;
		case 4:
			searchFile();
			break;
		default:
			break;
		}
	}

	public void run() {
		// Display welcome screen
		FileHandlingDisplay displayOptions = new FileHandlingDisplay();
		displayOptions.welcomeDisplay();

		// Perform user's chosen action
		int choice;
		do {
			// Display the main screen
			displayOptions.mainDisplay();

			// Take input from user for user's choice of action
			do {
				choice = chooseAction(MAIN_DISPLAY_CHOICES);
			} while (choice == -1);

			// Perform user's chosen action
			if (choice != 0) {
				performAction(choice);
			}
		} while (choice != 0);

		// User chose to exit the application
		System.out.println("\nApplication is closed. Goodbye!");
	}

}
