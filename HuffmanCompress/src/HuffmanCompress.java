import java.io.*;
import java.util.ArrayList;

public class HuffmanCompress {
    public static int symbolLimit;
    public static int bits;

    public static void main(String[] args) throws IOException {

        System.out.println(args[0] + " " + args[1] + " " + args[2]);
        //Required two files: input and output
        if (args.length != 3) {
            System.out.println("Error: arguments not specified");
            System.exit(1);
            return;
        } else if(Integer.valueOf(args[0]) > 24){
            System.out.println("Error: word too long");
            System.exit(1);
        }

        try {
            bits = Integer.valueOf(args[0]);
            symbolLimit = (int)Math.pow(2,bits);
            //System.out.println(symbolLimit);
            File inputFile = new File(args[1]);
            File outputFile = new File(args[2]);

            Frequencies freqs = Frequencies.getFrequencies(inputFile, bits);//Gaunami faile esanciu simboliu dazniai
            freqs.increment(symbolLimit);//symbolLimit - EOF, pridedamas EOF prie dazniu lenteles

            //freqs.printFreqs();


            CodeTree code = CodeTree.buildCodeTree(freqs); //Sudaromas kodu medis


            System.out.println("length: "+freqs.frequencies.length);
            CanonicalCode canonCode = new CanonicalCode(code);
            code = canonCode.toCodeTree();


            try (InputStream in = new BufferedInputStream(new FileInputStream(inputFile))) {
                try (BitOutputStream out = new BitOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
                    writeCodeLengthTable(out, canonCode);
                    compress(code, in, out);
                }
            }


        }catch(Exception e){
            e.printStackTrace();
        }
    }


    static void writeCodeLengthTable(BitOutputStream out, CanonicalCode canonCode) throws IOException {
        for (int i = 0; i < symbolLimit+1; i++) {
            int val = canonCode.getCodeLength(i);
            //System.out.println("val: " + val);

            // Write value as 8 bits in big endian
            for (int j = bits-1; j >= 0; j--) {
                out.write((val >>> j) & 1); //Pasiimamas tik vienas bitas ir irasomas. Ima tik po viena bita is val
                //System.out.println("Bit: "+((val >>> j) & 1));
            }
        }
    }

    static void compress(CodeTree code, InputStream in, BitOutputStream out) throws IOException {
        HuffmanEncoder enc = new HuffmanEncoder(out);
        enc.codeTree = code;
        while (true) {
            int b = in.read();
            //System.out.println("b: "+b);
            if (b == -1)
                break;
            enc.write(b);
            //System.out.println("b: "+b);
        }
        enc.write(symbolLimit);  // EOF
    }

}
