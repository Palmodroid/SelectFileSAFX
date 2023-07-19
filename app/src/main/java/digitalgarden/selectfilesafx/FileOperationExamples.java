package digitalgarden.selectfilesafx;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

class FileOperationExamples
    {

    /**
     * Sample method to read content of file uri using BufferedReader and InputStreamReader
     * @param context application context
     * @param uri uri of the file
     * @return content or some infomation about errors
     */
    public static String readFileContent(Context context, Uri uri)
        {
        // https://docs.oracle.com/javase/7/docs/technotes/guides/language/try-with-resources.html
        try ( BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        context.getContentResolver().openInputStream(uri))))
            {
            StringBuilder stringBuilder = new StringBuilder();
            String currentline;
            while ((currentline = reader.readLine()) != null)
                {
                stringBuilder.append(currentline).append("\n");
                }
            return stringBuilder.toString();
            }
        catch (FileNotFoundException e)
            {
            return "FILE NOT FOUND";
            }
        catch (IOException e)
            {
            return "I/O ERROR";
            }

        /* THIS IS JIGREADER TEST

        try (JigReader jigReader =
                     new JigReader(new FileInputStream
                             (context.getContentResolver().openFileDescriptor(uri, "rw")
                                     .getFileDescriptor())))
            {
            StringBuilder sb = new StringBuilder();

            Log.d("FILEOPERATIONS", "*ReadByte test");
            for(int n=0; n<12; n++)
                {
                int chr = jigReader.readByte();
                if ( chr>=0 )
                    sb.append( (char)chr );
                }
            sb.append("*");

            Log.d("FILEOPERATIONS", "*ReadByteBackward test");
            for(int n=0; n<14; n++)
                {
                int chr = jigReader.readByteBackward();
                if ( chr>=0 )
                    sb.append( (char)chr );
                }
            sb.append("*");

            Log.d("FILEOPERATIONS", "*ReadByte test");
            for(int n=0; n<12; n++)
                {
                int chr = jigReader.readByte();
                if ( chr>=0 )
                    sb.append( (char)chr );
                }
            sb.append("*");

            return sb.toString();
            }
        catch (FileNotFoundException e)
            {
            Log.e("ERROR", "File not found");
            }
        catch (IOException e)
            {
            Log.e("ERROR", "I/O error");
            }
        return "Error";

        */


        }


    /**
     * Sample method to write content to file uri using BufferedWriter and OutputStreamWriter
     * @param context application context
     * @param uri uri of the file
     * @param content String content to write to the file
     * @return content or some infomation about errors
     */
    public static String writeFileContent(Context context, Uri uri, String content)
        {
        try ( BufferedWriter writer = new BufferedWriter
                ( new OutputStreamWriter
                        ( context.getContentResolver().openOutputStream( uri ))))
            {
            writer.write( content );
            return content;
            }

            /* Writer is character based (with encodings), while Stream is binary, data should be
             * encoded previously

            ParcelFileDescriptor parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream =
                    new FileOutputStream(parcelFileDescriptor.getFileDescriptor());

            fileOutputStream.write( content.getBytes());

            fileOutputStream.close();
            parcelFileDescriptor.close();
             */

        catch (FileNotFoundException e)
            {
            return "FILE NOT FOUND";
            }
        catch (IOException e)
            {
            return "I/O ERROR";
            }
        }

    }
