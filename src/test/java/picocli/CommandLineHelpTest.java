/*
   Copyright 2017 Remko Popma

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package picocli;

import org.junit.Ignore;
import org.junit.Test;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Column;
import picocli.CommandLine.Help.TextTable;
import picocli.CommandLine.Option;
import picocli.CommandLine.Usage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.String;
import java.util.Iterator;

import static java.lang.String.format;
import static org.junit.Assert.*;

/**
 * Tests for picoCLI's "Usage" help functionality.
 */
public class CommandLineHelpTest {

    @Test
    @Ignore("requires support for detailedUsage")
    public void testUsageAnnotationDetailedUsage() throws Exception {
        @Usage(detailedUsage = true)
        class Params {
            @Option(names = {"-f", "--file"}, required = true, description = "the file to use")
            File file;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.usage(Params.class, new PrintStream(baos, true, "UTF8"));
        String result = baos.toString("UTF8");
        String programName = "<main class>"; //Params.class.getName();
        assertEquals(format("" +
                        "Usage: java %s -f <file>%n" +
                        "  -f, --file                  the file to use                                   %n",
                programName), result);
    }

    @Test
    public void testTextTable() {
        TextTable table = new TextTable();
        table.addRow("-v", ",", "--verbose", "show what you're doing while you are doing it");
        table.addRow("-p", null, null, "the quick brown fox jumped over the lazy dog. The quick brown fox jumped over the lazy dog.");
        assertEquals(String.format(
                "  -v, --verbose               show what you're doing while you are doing it     %n" +
                        "  -p                          the quick brown fox jumped over the lazy dog. The %n" +
                        "                                quick brown fox jumped over the lazy dog.       %n"
                ,""), table.toString(new StringBuilder()).toString());
    }

    @Test
    public void testTextTableAddsNewRowWhenTooManyValuesSpecified() {
        TextTable table = new TextTable();
        table.addRow("-c", ",", "--create", "description", "INVALID", "Row 3");
        assertEquals(String.format("" +
                        "  -c, --create                description                                       %n" +
                        "                                INVALID                                         %n" +
                        "                                Row 3                                           %n"
                ,""), table.toString(new StringBuilder()).toString());
    }

    @Test
    public void testTextTableAddsNewRowWhenAnyColumnTooLong() {
        TextTable table = new TextTable();
        table.addRow("-c", ",",
                "--create, --create2, --create3, --create4, --create5, --create6, --create7, --create8",
                "description", "INVALID", "Row 3");
        assertEquals(String.format("" +
                        "  -c, --create, --create2, --create3, --create4, --create5, --create6, --       %n" +
                        "        create7, --create8                                                      %n" +
                        "                              description                                       %n" +
                        "                                INVALID                                         %n" +
                        "                                Row 3                                           %n"
                ,""), table.toString(new StringBuilder()).toString());
    }

    @Test
    public void testCatUsageFormat() {
        @Usage(programName = "cat",
                summary = "Concatenate FILE(s), or standard input, to standard output.",
                footer = "Copyright(c) 2017")
        class Cat {
            @CommandLine.Parameters(description = "Files whose contents to display")
            @Option(names = "--help",    help = true,     description = "display this help and exit") boolean help;
            @Option(names = "--version", help = true,     description = "output version information and exit") boolean version;
            @Option(names = "-u",                         description = "(ignored)") boolean u;
            @Option(names = "-t",                         description = "equivalent to -vT") boolean t;
            @Option(names = "-e",                         description = "equivalent to -vET") boolean e;
            @Option(names = {"-A", "--show-all"},         description = "equivalent to -vET") boolean showAll;
            @Option(names = {"-s", "--squeeze-blank"},    description = "suppress repeated empty output lines") boolean squeeze;
            @Option(names = {"-v", "--show-nonprinting"}, description = "use ^ and M- notation, except for LDF and TAB") boolean v;
            @Option(names = {"-b", "--number-nonblank"},  description = "number nonempty output lines, overrides -n") boolean b;
            @Option(names = {"-T", "--show-tabs"},        description = "display TAB characters as ^I") boolean T;
            @Option(names = {"-E", "--show-ends"},        description = "display $ at end of each line") boolean E;
            @Option(names = {"-n", "--number"},           description = "number all output lines") boolean n;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CommandLine.usage(Cat.class, new PrintStream(baos));
        String expected = String.format(
                "Usage: cat [OPTIONS] [PARAMETERS]%n" +
                        "Concatenate FILE(s), or standard input, to standard output.%n" +
                        "  -A, --show-all              equivalent to -vET                                %n" +
                        "  -b, --number-nonblank       number nonempty output lines, overrides -n        %n" +
                        "  -e                          equivalent to -vET                                %n" +
                        "  -E, --show-ends             display $ at end of each line                     %n" +
                        "  -n, --number                number all output lines                           %n" +
                        "  -s, --squeeze-blank         suppress repeated empty output lines              %n" +
                        "  -t                          equivalent to -vT                                 %n" +
                        "  -T, --show-tabs             display TAB characters as ^I                      %n" +
                        "  -u                          (ignored)                                         %n" +
                        "  -v, --show-nonprinting      use ^ and M- notation, except for LDF and TAB     %n" +
                        "      --help                  display this help and exit                        %n" +
                        "      --version               output version information and exit               %n" +
                        "Copyright(c) 2017%n", "");
        assertEquals(expected, baos.toString());
    }

    @Test
    public void testZipUsageFormat() {
        @Usage(summary = {
                "Copyright (c) 1990-2008 Info-ZIP - Type 'zip \"-L\"' for software license.",
                "Zip 3.0 (July 5th 2008). Usage:",
                "zip [-options] [-b path] [-t mmddyyyy] [-n suffixes] [zipfile list] [-xi list]",
                "  The default action is to add or replace zipfile entries from list, which",
                "  can include the special name - to compress standard input.",
                "  If zipfile and list are omitted, zip compresses stdin to stdout."}
        )
        class Zip {
            @Option(names = "-f", description = "freshen: only changed files") boolean freshen;
            @Option(names = "-u", description = "update: only changed or new files") boolean update;
            @Option(names = "-d", description = "delete entries in zipfile") boolean delete;
            @Option(names = "-m", description = "move into zipfile (delete OS files)") boolean move;
            @Option(names = "-r", description = "recurse into directories") boolean recurse;
            @Option(names = "-j", description = "junk (don't record) directory names") boolean junk;
            @Option(names = "-0", description = "store only") boolean store;
            @Option(names = "-l", description = "convert LF to CR LF (-ll CR LF to LF)") boolean lf2crlf;
            @Option(names = "-1", description = "compress faster") boolean faster;
            @Option(names = "-9", description = "compress better") boolean better;
            @Option(names = "-q", description = "quiet operation") boolean quiet;
            @Option(names = "-v", description = "verbose operation/print version info") boolean verbose;
            @Option(names = "-c", description = "add one-line comments") boolean comments;
            @Option(names = "-z", description = "add zipfile comment") boolean zipComment;
            @Option(names = "-@", description = "read names from stdin") boolean readFileList;
            @Option(names = "-o", description = "make zipfile as old as latest entry") boolean old;
            @Option(names = "-x", description = "exclude the following names") boolean exclude;
            @Option(names = "-i", description = "include only the following names") boolean include;
            @Option(names = "-F", description = "fix zipfile (-FF try harder)") boolean fix;
            @Option(names = "-D", description = "do not add directory entries") boolean directories;
            @Option(names = "-A", description = "adjust self-extracting exe") boolean adjust;
            @Option(names = "-J", description = "junk zipfile prefix (unzipsfx)") boolean junkPrefix;
            @Option(names = "-T", description = "test zipfile integrity") boolean test;
            @Option(names = "-X", description = "eXclude eXtra file attributes") boolean excludeAttribs;
            @Option(names = "-y", description = "store symbolic links as the link instead of the referenced file") boolean symbolic;
            @Option(names = "-e", description = "encrypt") boolean encrypt;
            @Option(names = "-n", description = "don't compress these suffixes") boolean dontCompress;
            @Option(names = "-h2", description = "show more help") boolean moreHelp;
        }
        String expected  = String.format("" +
                "Copyright (c) 1990-2008 Info-ZIP - Type 'zip \"-L\"' for software license.%n" +
                "Zip 3.0 (July 5th 2008). Usage:%n" +
                "zip [-options] [-b path] [-t mmddyyyy] [-n suffixes] [zipfile list] [-xi list]%n" +
                "  The default action is to add or replace zipfile entries from list, which%n" +
                "  can include the special name - to compress standard input.%n" +
                "  If zipfile and list are omitted, zip compresses stdin to stdout.%n" +
                "  -f   freshen: only changed files  -u   update: only changed or new files    %n" +
                "  -d   delete entries in zipfile    -m   move into zipfile (delete OS files)  %n" +
                "  -r   recurse into directories     -j   junk (don't record) directory names  %n" +
                "  -0   store only                   -l   convert LF to CR LF (-ll CR LF to LF)%n" +
                "  -1   compress faster              -9   compress better                      %n" +
                "  -q   quiet operation              -v   verbose operation/print version info %n" +
                "  -c   add one-line comments        -z   add zipfile comment                  %n" +
                "  -@   read names from stdin        -o   make zipfile as old as latest entry  %n" +
                "  -x   exclude the following names  -i   include only the following names     %n" +
                "  -F   fix zipfile (-FF try harder) -D   do not add directory entries         %n" +
                "  -A   adjust self-extracting exe   -J   junk zipfile prefix (unzipsfx)       %n" +
                "  -T   test zipfile integrity       -X   eXclude eXtra file attributes        %n" +
                "  -y   store symbolic links as the link instead of the referenced file        %n" +
                "  -e   encrypt                      -n   don't compress these suffixes        %n" +
                "  -h2  show more help              %n", "");
        Help help = new Help(Zip.class);
        StringBuilder sb = new StringBuilder();
        help.appendSummary(sb);
        TextTable textTable = new TextTable(new Column(5, 2),
                                            new Column(30, 2),
                                            new Column(4, 1),
                                            new Column(39, 2));
        Iterator<Option> iter = help.option2Field.keySet().iterator();
        while (iter.hasNext()) {
            Option option = iter.next();
            if (option.hidden()) { continue; }
            String[] names = option.names();

            String name2 = "";
            String description2 = "";
            if (iter.hasNext()) {
                Option option2 = iter.next();
                while (option2.hidden() && iter.hasNext()) {
                    option2 = iter.next();
                }
                if (!option2.hidden()) {
                    name2 = option2.names()[0];
                    description2 = option2.description();
                }
            }
            textTable.addRow(names[0], option.description(), name2, description2);
        }
        textTable.toString(sb);
        assertEquals(expected, sb.toString());
    }
}
