# FixFileHistory

Author: Luis F. Bustamante
contact: luisfebusta@gmail.com

USAGE:
	FixNames [options] <src_directory> <dest_directory>

	src_directory:	Is the directory that contains the original files with the
			File History times stamp appended at the end
	dest_directory:
			Specifies the directory to which the files
			with the new names will be copied to.
			If target_directory is specified src_directory will be left intact
OPTIONS:
	-h --help	Shows this screen
	--merge		Will restore files and Directory structure that DOESN'T exist on target directory
			witht he resume flag, the program will navigate the directory structure
	--resume		If the programm stopped execution you can re-issue the command
			witht he resume flag, the program will navigate the directory structure
			and resume copying from where it left off
	-v		Prints to the screen all the renames/movers as they're being done
	-t --test	Does a test run if used with --merge, it will print out only files which would not be copied 
			if used with -v it would print all copy/skip operations that would be done,
	ut doesn't change anything (for the cautious user) use with 

DESCRIPTION:

	Utility to restore latest copy of file history files - to be used if file history won't recognize your backup directory

	The utility will restore most resent copy of files to a new Directory. The new directory will be marked as read only, you must change this by right clicking the directory and unchecking the read-only checkbox on the properties window.

	When specifying the --resume or --merge flag only Files and directories that do not exist in target directory will be restored.

	To run the utility download the Project and make sure to preserve the directory structure. You must also have Java installed on your computer

	From within the Project's parent folder:

	java -cp bin FixFileHistory.FixFileHistory [options] <src_directory> <dest_directory>

HOW TO MONITOR PROGRESS:

	If running with -v flag the list of actions being performed will be displayed in the command prompt window, as files are moved the information displayed on the screen will be updated.

	Windows Task Manager (CTRL + ALT + DELETE) can be used to monitor progress. Look for the java process, you will see a high Disk usage reported for this process, which means the utility is doing it's job

NOTES:
	If for some reason the Disk % usage drops to 0% or the actions displayed with the -v flag are taking long to update, hit enter a few times on the command prompt window (That worked for me)

	Lastly, it is recommended that power options are changed so that the computer doesn't go to sleep as the restore operations might take some time.
	
	GUI front end coming in the next few weeks.
	
	Any questions or suggestions shoot me an email with the subject "FixFileHistory Utility"

SAMPLE RUN:

	Note FixFileHistory is the root directory for the project

	C:\Users\Luis F\workspace\FixFileHistory>java -Xms4G -cp bin\ FixFileHistory.FixFileHistory -v "K:\FileHistory\Luis F\LUISANDDAYA-PC\Data\C\Users\Luis F" "C:\Users\Public\Luis F Restore"

FUTURE ADDITIONS:

	GUI front end, build file, potentially package utility into executable.
