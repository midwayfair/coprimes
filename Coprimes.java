/*
 COSC 236
 Jon S. Patton
 Description: Generate output files populated with primes and co-primes
 Filename: Coprimes.java
 Date started: 6/28/17
 Modification history: 6/28/17
 Methods: main, fvGeneratePrimeList, fvGenerateCoPrimeList, fvPrintArrayToFile
  fsGetFileName, fsGetDataType
Bonus class: singleton console scanner
 */

import java.util.*;
import java.io.*;

public class Coprimes
{
  public static void main (String args[])
  {
    while (fbMenu()){} //giggle.

    System.out.println("Goodbye.");
  }

  public static boolean fbMenu()
  {
    String sUsage;

    System.out.println("This program can:");
    System.out.println("1: Generate a list of prime numbers in a given range.");
    System.out.println("2: Generate a list of co-prime numbers in a given range.");
    System.out.print("Please make a selection (1, 2, or q to quit) -> ");
    sUsage = UserInput.get(); //Explained way down at the bottom of the file.
    System.out.println();

    try
    {
      if (sUsage.equalsIgnoreCase("q") || sUsage.equalsIgnoreCase("quit") || Integer.valueOf(sUsage) <= 0)
      {
        return false;
      }
      else if (sUsage.equals("1"))
      {
        fvGeneratePrimeList();
        return fbMenu();
      }
      else if (sUsage.equals("2"))
      {
        fvGenerateCoPrimeList();
        return fbMenu();
      }
      else
      {
        throw new IOException();
      }
    }
    catch (IOException | NumberFormatException sErr)
    {
      System.err.println("Error: Invalid selection.");
      return fbMenu(); //Recurses the menu if they made a mistake
    }
  }

  //A method to print all primes in a given range to a file.
  public static void fvGeneratePrimeList()
  {
    int iMin; //Min and max range values for the list of primes
    int iMax;

    System.out.println("Generates a text file filled with primes in a given range.");
    System.out.println("Valid range is between 2 (minimum) and 2^32 - 1 (max ... if you have 2GB+ memory free).");
    System.out.println("Negative value at any time quits without generating the file.");
    System.out.println();

    try
    {
      if ((iMin = fiGetMinOrMax("min")) < 0) //hehehe.
      {
        return;
      }

      if ((iMax = fiGetMinOrMax("max")) < 0) //I can't believe this works.
      {
        return;
      }
      if (iMax < iMin)
      {
        throw new IOException();
      }

      //Sieve some primes.
      int iPrimes[] = fiPrimeSieve(iMin, iMax);
      fvPrintArrayToFile(iPrimes, "primes.txt", "grid"); //second and third arguments can be hard-coded file info
    }
    catch (IOException | NumberFormatException sErr)
    {
      System.err.println("Error: Invalid entry.");
      fvGeneratePrimeList();
    }
  }

  //Returns an interger array object populated with all prime numbers below a
  //given ceiling.
  public static int[] fiPrimeSieve(int piMin, int piMax)
  {
    //The build array is boolean because so takes up less space in memory ... hypothetically
    //Java apparently doesn't always do that, which is dumb. (C and C++ and even Python guarantee byte arrays.)
    boolean bSieve[] = new boolean[piMax + 1];

    int iTemp; //working value while populating
    int iIt; //loop iterator
    int iTally; //keeps track of how many primes we've found
    int index; //where we are in the temporary array during the build loop
    int iCeiling; //the last number we'll check in the array.

    iTally = 0;
    index = 0;
    iCeiling = (int) Math.sqrt(piMax); //We don't need to exceed the sqrt of iMax.

    for (iIt = 2; iIt <= iCeiling; iIt++)
    {
      if (!bSieve[iIt])
      {
        //flip all elements of the array that are multiples of the current index
        for (iTemp = iIt; iTemp * iIt <= piMax; iTemp++)
        {
          bSieve[iTemp * iIt] = true;
        }
      }
    }

    //Count up the primes. Unfortunately there's not a more efficient way to do this in Java,
    //because we have to know how big the actual prime number array will be.
    for (iIt = 2; iIt <= piMax; iIt++)
    {
      if (!bSieve[iIt] && iIt >= piMin && iIt <= piMax)
        iTally++;
    }

    //Now build an array of longs from the *false* values.
    //The array is declared down here because we didn't know how long it was until now.
    int iPrimes[] = new int[iTally];

    for (iIt = 2; iIt <= piMax; iIt++)
    {
      if (iIt >= piMin && !bSieve[iIt])
      {
        iPrimes[index] = iIt;
        index++;
      }
    }
    return iPrimes;
  }

  //Generates a list of all numbers that are co-prime for a given range.
  public static void fvGenerateCoPrimeList()
  {
    int iMin; //Min and max values for the range of co-primes generated.
    int iMax;

    System.out.println("Generates a text file filled with co-primes in a given range.");
    System.out.println("Valid range is between 2 (minimum) and 2^32 - 1 (max).");
    System.out.println("Negative value at any time quits without generating the file.");
    System.out.println();

    try
    {
      if ((iMin = fiGetMinOrMax("min")) < 0)
      {
        return;
      }

      if ((iMax = fiGetMinOrMax("max")) < 0)
      {
        return;
      }

      if (iMax < iMin)
      {
        throw new IOException();
      }

      //Find some co-primes
      String sCoPrimes[] = fiCoPrimeGenerator(iMin, iMax);
      fvPrintArrayToFile(sCoPrimes, "coprimes.txt", "line"); //second and third arguments can be hard-coded file info
    }
    catch (IOException | NumberFormatException sErr)
    {
      System.err.println("Error: Invalid entry.");
      fvGenerateCoPrimeList();
    }
  }

  //Generates a list of co-primes of each number in a given range.
  public static String[] fiCoPrimeGenerator(int piMin, int piMax)
  {
    //Some housekeeping before we get to the meat of this.
    //0 and 1 have no/infinite co-primes, so just make sure nothing below 2 gets in.
    if (piMin < 2)
    {
      piMin = 2;
    }

    //If the method was passed the range elements in the wrong order, fix that.
    if (piMin > piMax)
    {
      int iTemp = piMin;
      piMin = piMax;
      piMax = iTemp;
    }

    int iIt; //Outter loop iterator
    int iJ; //Inner loop iterator
    String sCoPrimes[] = new String[piMax - piMin + 1]; //Bounds-inclusive, so +1 on the size

    //Find all co-primes in the range of each number in the range.
    for (iIt = piMin; iIt <= piMax; iIt++)
    {
      sCoPrimes[iIt - piMin] = "Co primes of " + iIt + ": ";
      for (iJ = 2; iJ <= piMax; iJ++)
      {
        //Two numbers are co-prime if their gcf is 1.
        if (fiGCD(iIt, iJ) == 1)
        {
          sCoPrimes[iIt - piMin] = sCoPrimes[iIt - piMin] + Integer.toString(iJ) + ", ";
        }
      }

      //fix some output issues (deletes a trailing ", " and turns it into a hard return).
      if (sCoPrimes[iIt - piMin].endsWith(" "))
      {
        sCoPrimes[iIt - piMin] = sCoPrimes[iIt - piMin].substring(0, sCoPrimes[iIt - piMin].length() - 2) + "\n";
      }
    }

    return sCoPrimes;
  }

  //Class naming standards make this awkward since it's usually written a, b
  public static int fiGCD(int piA, int piB)
  {
    int iTemp; //Used to store b temporarily

    //GCD shan't be negative.
    if (piA < 0)
    {
      piA = -piA;
    }
    if (piB < 0)
    {
      piB = -piB;
    }

    //This is the non-recursive algorithm.
    //Store b temporarily, make b the remainder of a/b, replace a with the old b
    //Stop when the remainder was 0 and a will be the gcf.
    while (piB != 0)
    {
      iTemp = piB;
      piB = piA % piB;
      piA = iTemp;
    }
    return piA;
  }

  //Prints an array to a file
  //Special handling for CSV, text, or numerical
  //This is another case where a custom class could be set up to handle different types of arrays.
  //As it stands, this is super ugly because there's a lot of copypaste between the codes.
  //The *actual* way to handle this might not even be covered in this course.

  //"o" for object, by the way.
  public static void fvPrintArrayToFile(int poArray[], String psFileName, String psDataType)
  {
    String sFileName; //The parsed or generated filename
    String sDataType; //The parsed or generated output specs
    int iIt; //Loop iterator

    try
    {
      sFileName = fsGetFileName(psFileName);
      //if they quit, exit the method.
      if (sFileName.isEmpty() || sFileName.equalsIgnoreCase("q") || sFileName.equalsIgnoreCase("quit"))
      {
        return;
      }

      sDataType = fsGetDataType(psDataType);
      if (sDataType.equalsIgnoreCase("q") || sDataType.equalsIgnoreCase("quit") || sDataType.isEmpty())
      {
        return;
      }

      PrintStream ofsOutput = new PrintStream(new File(sFileName));

      for (iIt = 0; iIt < poArray.length; iIt++)
      {
        //Comma-separated values
        if (sDataType.equalsIgnoreCase("csv"))
        {
          if (iIt != poArray.length - 1)
          {
            ofsOutput.print(poArray[iIt] + ", ");
          }
          else
          {
            ofsOutput.print(poArray[iIt]);
          }
        }

        //Space-separated values
        if (sDataType.equalsIgnoreCase("txt"))
        {
          if (iIt != poArray.length - 1)
          {
            ofsOutput.print(poArray[iIt] + " ");
          }
          else
          {
            ofsOutput.print(poArray[iIt]);
          }
        }

        //Line-separated values
        if (sDataType.equalsIgnoreCase("line"))
        {
          ofsOutput.println(poArray[iIt]);
        }

        //Numerical grid
        if (sDataType.equalsIgnoreCase("grid"))
        {
          try
          {
            if (iIt % 5 == 0)
              ofsOutput.println();
            ofsOutput.printf("%12d", poArray[iIt]);
          }
          catch (IllegalArgumentException sErr)
          {
            System.err.println("Error: invalid data type for numerical grid.");
            return;
          }
        }
      }
    }
    catch (IOException sErr)
    {
      System.err.println("Error: could not generate output file.");
      fvPrintArrayToFile(poArray, "", "");
    }
  }

  //Prints a STRING array to a file.
  public static void fvPrintArrayToFile(String poArray[], String psFileName, String psDataType)
  {
    String sFileName; //The parsed or generated file name
    String sDataType; //The parsed or generated output specs
    int iIt; //Loop iterator

    try
    {
      sFileName = fsGetFileName(psFileName);
      //if they quit, exit the method.
      if (sFileName.isEmpty() || sFileName.equalsIgnoreCase("q") || sFileName.equalsIgnoreCase("quit"))
      {
        return;
      }

      sDataType = fsGetDataType(psDataType);
      if (sDataType.equalsIgnoreCase("q") || sDataType.equalsIgnoreCase("quit") || sDataType.isEmpty())
      {
        return;
      }

      PrintStream ofsOutput = new PrintStream(new File(sFileName));

      for (iIt = 0; iIt < poArray.length; iIt++)
      {
        //Comma-separated values
        if (sDataType.equalsIgnoreCase("csv"))
        {
          if (iIt != poArray.length - 1)
          {
            ofsOutput.print(poArray[iIt] + ", ");
          }
          else
          {
            ofsOutput.print(poArray[iIt]);
          }
        }

        //Space-separated values
        if (sDataType.equalsIgnoreCase("txt"))
        {
          if (iIt != poArray.length - 1)
          {
            ofsOutput.print(poArray[iIt] + " ");
          }
          else
          {
            ofsOutput.print(poArray[iIt]);
          }
        }

        //Line-separated values
        if (sDataType.equalsIgnoreCase("line"))
        {
          ofsOutput.println(poArray[iIt]);
        }

        //Numerical grid
        if (sDataType.equalsIgnoreCase("grid"))
        {
          try
          {
            if (iIt % 5 == 0)
              ofsOutput.println();
            ofsOutput.printf("%12f", poArray[iIt]);
          }
          catch (IllegalArgumentException sErr)
          {
            System.err.println("Error: invalid data type for numerical grid.");
          }
        }
      }
    }
    catch (IOException sErr)
    {
      System.err.println("Error: could not generate output file.");
      fvPrintArrayToFile(poArray, "", "");
    }
  }

  //Gets a file name or parses it from an argument.
  public static String fsGetFileName(String psFileName)
  {
    if (psFileName.isEmpty())
    {
      System.out.print("Please enter a name for the output file (Enter or q quits): ");
      psFileName = UserInput.get();
    }
    else
    {
      psFileName = psFileName;
    }
    return psFileName;
  }

  //Obtains the file separation type for the print-to-file or parses it from the argument.
  public static String fsGetDataType(String psDataType)
  {
    if (psDataType.isEmpty())
    {
      System.out.println("1 or csv: comma separated values;");
      System.out.println("2 or txt. Space-separated values;");
      System.out.println("3 or grid. A grid of numbers, right justified;");
      System.out.println("4 or line. Line-separated values (all values on separate lines) (this is the default)");
      System.out.println("Enter or q quits.");
      System.out.print("-> ");
      psDataType = UserInput.get();
      System.out.println();

      if (psDataType.equals("1"))
      {
        psDataType = "csv";
      }
      else if (psDataType.equals("2"))
      {
        psDataType = "txt";
      }
      else if (psDataType.equals("3"))
      {
        psDataType = "grid";
      }
      else if (psDataType.equalsIgnoreCase("q") || psDataType.equalsIgnoreCase("quit") || psDataType.isEmpty())
      {
        return "q";
      }
      else
      {
        psDataType = "line";
      }
    }
    else
    {
      psDataType = psDataType;
    }
    return psDataType;
  }

  public static int fiGetMinOrMax(String psUse)
  {
    int iVal; //User input value.
    try
    {
      if (psUse.equalsIgnoreCase("min"))
      {
        System.out.print("Enter the lowest value for the range. -> ");
      }

      else if (psUse.equalsIgnoreCase("max"))
      {
        System.out.print("Enter the largest value for the range. -> ");
      }
      iVal = Integer.parseInt(UserInput.get());
      System.out.println();

      return iVal;
    }
    catch (NumberFormatException sErr)
    {
      System.err.println("Integers only, please.");
      return fiGetMinOrMax(psUse);
    }
  }
}

/*This is a Singleton, which is a Java-ish way of handling a global In-Out.
 * https://en.wikipedia.org/wiki/Singleton_pattern (read the pros and cons there)
 * This is one way to avoid creating a new scanner object in a recursive menu.
 */

final class UserInput
{
  public static final Scanner CONSOLE = new Scanner(System.in);

  public static String get()
  {
    return CONSOLE.nextLine();
  }
}
