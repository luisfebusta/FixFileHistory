// Created by Luis F. Bustamante
// luisfebusta@gmail.com

package FixFileHistory;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import static java.nio.file.StandardCopyOption.*; //File operation options
import static java.nio.file.LinkOption.*; //Link options


public class FixFileHistory {
    
    private Path src;
    private Path dest;
    private boolean v;
    private boolean t;
    private boolean r;
    private int copied;
    private int olderVersions;
    private int alreadyExist;
    
    public FixFileHistory()
    {
        src = null;
        dest = null;
        v = false;
        t = false;
        r = false;
        copied = 0;
        olderVersions = 0;
        alreadyExist = 0;
    }
    
    public void setVerbose()
    {
        v = true;
    }
    
    public void setTestRun()
    {
        t = true;
    }
    
    public void setResume()
    {
        r = true;
    }
    public void pvln(String txt)
    {
        if(v)
            System.out.println(txt);
    }
    
    public void pv(String txt)
    {
        if(v)
            System.out.print(txt);
    }
    
    /**
	 * @return the copied
	 */
	public int getCopied() {
		return copied;
	}
	
	/**
	 * @return the not_copied
	 */
	public int getOlderVersions() {
		return olderVersions;
	}
	
	public int getAlreadyExists() 
	{
		return alreadyExist;
	}
	
    public void setSrc(String src)
    {
        if(src == null)
        {
            throw new NullPointerException("Source directory cannot be null");
        }
        Path tmp = Paths.get(src);
        if (!Files.exists(tmp))
        {
            throw new IllegalArgumentException("Source directory \"" + src + "\" does not exist");
        }
        if (!Files.isDirectory(tmp))
        {
            throw new IllegalArgumentException(src + " is not a directory");
        }
        this.src = tmp;
        pvln("Source Directory: " + src);
    }
    
    public void setDst(String dest)
    {
        if(src == null)
        {
            throw new NullPointerException("Target directory must not be null");
        }
        Path tmp = Paths.get(dest);
        if (Files.exists(tmp) && !r)
        {
            throw new IllegalArgumentException("Target directory \"" + dest + "\" already exist");
        }
        
        this.dest = tmp;
        pvln("Destination Directory: " + dest);
    }
    
    // Recursive approach, might change later if needed
    public void execute()
    {
        if (src == null || dest == null)
        {
            throw new IllegalArgumentException(
                    "source_directory and destination directory must be specified");
        }
        
        try {
            if (Files.exists(dest) && Files.isSameFile(src, dest))
            {
                throw new IllegalArgumentException(
                        "source_directory and destination directory must be different");
            }
        } catch (java.nio.file.NoSuchFileException e) {
            
            //do nothing.
            System.out.println("FATAL ERROR: source_directory does not exist");
            System.exit(Integer.MAX_VALUE);
            
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
            //e.printStackTrace();
        } 
        
        pvln("Begin file copy");
        copy(this.src, this.dest);

    }
    
    private void copy(Path src, Path dest)
    {
        //create destination directory;
        pvln("Creating directory " + dest);
        
        if (Files.exists(dest) && !r)
        {
            System.err.println("The Impossible has happened!!! (Or most likely I messed up)");
            System.exit(Integer.MAX_VALUE);
        }
        
        if (!t && !Files.exists(dest)){
            //TODO: check attributes on src and dst directories
            try {
                Files.createDirectory(dest);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                System.out.println("ERROR: Cannot create target directory " + dest);
                return;

            }
        }
        DirectoryStream<Path> files;
        DirectoryStream<Path> directories;
        try {
            DirectoryStream.Filter<Path> onlyDirs = (Path p) -> (Files.isDirectory(p));
            DirectoryStream.Filter<Path> onlyFiles = (Path p) -> (!Files.isDirectory(p));

            files = Files.newDirectoryStream(src, onlyFiles);
            directories = Files.newDirectoryStream(src, onlyDirs);

            pvln("Copying directory contents: " + src + " to " + dest);
            
            //Process all files first
            HashMap<String, ArrayList<Path>> fileMap = new HashMap<>();
            for (Path f: files)
            {                               
                if (Files.isRegularFile(f, NOFOLLOW_LINKS) || Files.isSymbolicLink(f))
                {
                    //figure out if filename is in File History format and remove timestamp;
                    String newName = getOriginalName(f);
                    ArrayList <Path> list = fileMap.get(newName);
                    if(list == null )
                    {
                        list = new ArrayList<Path>();
                        fileMap.put(newName, list);
                    }
                    list.add(f);
                } 
                else if(Files.isDirectory(f))
                {
                    //If done right, this will never be reached, but it's here for error detection
                    System.out.println("ERROR: file "+ f.toString() + " Is not a file");
                }
            }
            
            for (Entry<String, ArrayList<Path>> ent: fileMap.entrySet())
            {
                // TODO:  Allow timestamp selection as opposed to always restoring the most recent copy. 
                // For now the utility just copies copy the most recent file by sorting the list of
                // "duplicate" files.
                Collections.sort(ent.getValue());
                Path f = ent.getValue().get(ent.getValue().size()-1);
                olderVersions += ent.getValue().size()-1;
                copyFile(f,Paths.get(dest.toString(),ent.getKey())); 
                
            }
            
            
            
            //process sub-directories
            for (Path f: directories)
            {
                if(Files.isDirectory(f))
                {
                    copy(f,Paths.get(dest.toString(),f.getFileName().toString()));
                }
                else
                {
                	// In theory this will not be reached...
                    System.out.println("ERROR: Directory "+ f.toString() + "Is not a directory");
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    private String getOriginalName(Path original)
    {
        String originalName = original.getFileName().toString();
        String newName = originalName;
        //TODO: replace with regex
        //TODO: check that value inside the parentheses is a time stamp
        StringBuilder sb = new StringBuilder(originalName.length());
        int dot = originalName.lastIndexOf('.');
        int closeParen = originalName.lastIndexOf(')');
        int openParen = originalName.lastIndexOf('(');
        if(openParen != -1 && closeParen != -1 && dot - closeParen == 1)
        {
            char [] on = originalName.toCharArray();
            sb.append(on, 0, openParen-1);
            sb.append(on,closeParen+1,on.length-closeParen-1);
            newName = sb.toString();
        }
        return newName;
    }
    
    private void copyFile(Path src, Path dest)
    {
        pv("Copying file " + src + " to " + dest);
        if (Files.exists(dest))
        {
        	System.out.println("Skipping File " + dest.toString() + " - already Exists");
        	alreadyExist++;
        	return;
        }
        if (t)
        {
        	copied++;
            return;
        }
        try {
          //Write File only if it doesn't exist;
            if(!Files.exists(dest, NOFOLLOW_LINKS));
                Files.copy(src, dest,COPY_ATTRIBUTES, NOFOLLOW_LINKS);
        } catch (IOException e) {
            
            System.out.println("Error copying file: " + e.getMessage());
        }
        
        //set file attributes
        try {
        	if(!changeAttributes(dest))
        	System.out.println("Couldn't modify read-only and archive attributes for file" + dest);
           
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        pvln(" - DONE!");
        copied ++;     
    }
    
    private boolean  changeAttributes(Path dest) throws IOException
    { 
    	// try{
    	//     Files.setAttribute(dest, "DOS:readonly", false);
    	// }
    	// catch (UnsupportedOperationException e) {
        //     pv("\tsetAttributest Unsupported");
        // }
    	Process p = Runtime.getRuntime().exec("attrib " + "" + dest.toAbsolutePath() + "" + "-A -R");
    	try {
			boolean success = p.waitFor(3,java.util.concurrent.TimeUnit.SECONDS);
			return success;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			return false;	
		}
    }
     
    private static void printUsage()
    {
        //String name = getClass()
        System.out.println("USAGE:");
        System.out.println("\tFixNames [options] <src_directory> <dest_directory>");
        System.out.println("");
        System.out.println("\tsrc_directory:\tIs the directory that contains the original files with the");
        System.out.println("\t\t\tFile History times stamp appended at the end");
        
        System.out.println("\tdest_directory:");
        System.out.println("\t\t\tSpecifies the directory to which the files");
        System.out.println("\t\t\twith the new names will be copied to.");
        System.out.println("\t\t\tIf target_directory is specified src_directory will be left intact");
        
        System.out.println("OPTIONS:");
        
        System.out.println("\t-h --help\tShows this screen");
        
        System.out.println("\t--merge\t\tWill restore files and Directory structure that DOESN'T exist on target directory");
        System.out.println("\t\t\twitht he resume flag, the program will navigate the directory structure");
        
        System.out.println("\t--resume\t\tIf the programm stopped execution you can re-issue the command");
        System.out.println("\t\t\twitht he resume flag, the program will navigate the directory structure");
        System.out.println("\t\t\tand resume copying from where it left off");
        
        System.out.println("\t-v\t\tPrints to the screen all the renames/movers as they're being done");
        
        System.out.println("\t-t --test\tDoes a test run if used with --merge, it will print out only files which would not be copied ");
        System.out.println("\t\t\tif used with -v it would print all copy/skip operations that would be done,");
        System.out.println("\t\t\but doesn't change anything (for the cautious user) use with ");
        System.exit(0);
    }
    

    
    public static void main(String [] args)
    {
        if (args.length == 0)
        {
            System.out.println("ERROR: Invalid number of arguments\n");
            printUsage();
        }
     
        FixFileHistory fn = new FixFileHistory();
     
        int i;
        parse_opts:
        for(i = 0; i < args.length; i++)
        {   
            switch(args[i])
            {
                case "--help":
                case "-h":
                    printUsage();
                    break;
                case "--merge": //fall through
                case "--resume":
                    fn.setResume();
                    break;
                case "-t":
                    fn.setTestRun();
                    //fn.setVerbose();
                    break;
                case "-v":
                    fn.setVerbose();
                    break;
                default:
                    if(args[i].charAt(0) == '-')
                    {
                        System.out.println("ERROR: Invalid Option "+ args[i]);
                        printUsage();
                    }
                    break parse_opts;
            }
        }
        try{
            if (args.length - i > 2)
            {
                System.out.println("ERROR: Invalid number of arguments");
                printUsage();
            }
            fn.setSrc(args[i++]);
            if (i < args.length)
                fn.setDst(args[i]);
        }
        catch(IllegalArgumentException | NullPointerException e)
        {
            System.out.println("ERROR: " + e.getMessage());
            //e.printStackTrace();
            printUsage();
        }
        
        try{
            fn.execute();
            //TODO: maybe change println to format
            System.out.println("DONE!!!");
            System.out.println("Files Copied:                                           \t"+fn.getCopied());
            System.out.println("Files Not Copied because they were older versions:      \t"+fn.getOlderVersions());
            System.out.println("Files not copied because they already existed on target:\t"+fn.getAlreadyExists());
        }
        finally
        {
            
        }
        
    }

	

}
