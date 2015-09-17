# FixFileHistory

Author: Luis F. Bustamante
contact: luisfebusta@gmail.com

Utility to restore latest copy of file history files - to be used if file history won't recognize your backup directory

The utility will restore most resent copy of files to a new Directory. The new directory will be marked as read only, you must change this by right clicking the directory and unchecking the read-only checkbox on the properties window.

When specifying the --resume flag only Files and directories that do not exist in the existing directory will be restored.

To run the utility download the Project and make sure to preserve the directory structure. You must also have Java installed on your computer

From within the Project's parent folder:

java -cp bin FixFileHistory.FixFileHistory [options] <src_directory> <dest_directory>

USAGE:
	FixFileHistory [options] <src_directory> <dest_directory>

	src_directory:	Is the directory that contains the original files with the
			File History times stamp appended at the end
	dest_directory:
			Specifies the directory to which the files
			with the new names will be copied to.
			If target_directory is specified src_directory will be left intact
OPTIONS:
	-h --help	Shows this screen
	--resume		If the programm stopped execution you can re-issue the command
			witht he resume flag, the program will navigate the directory structure
			and resume copying from where it left off
	-v		Prints to the screen all the renames/movers as they're being done
	-t --test	Does a test run printing all the renames and moves that would
			be done, but doesn't change anything (for the cautious user)

Future Versions: GUI front end, build file, potentially package utility into executable.

SAMPLE RUN:

Note FixFileHistory is the root directory for the project

C:\Users\Luis F\workspace\FixFileHistory>java -Xms4G -cp bin\ FixFileHistory.FixFileHistory -v "K:\FileHistory\Luis F\LUISANDDAYA-PC\Data\C\Users\Luis F" "C:\Users\Public\Luis F Restore"


