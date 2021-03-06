import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

public class Gravador {
    public static void salvaArquivo(Container container) {

        int tamanho = Bloco.byteToInt(Bloco.getBytes(container.controle.dados, 1, 3));

        byte[] bytes = new byte[tamanho + (container.blocos.size() * tamanho)];

        bytes = Bloco.bytePlusbyte(bytes, container.controle.dados, 0);

        for(int i = 0, j = 0; i < container.blocos.size(); i++, j += 8192 ) {
            bytes = Bloco.bytePlusbyte(bytes, container.blocos.get(i).dados, j);
        }


        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream("C:\\Users\\Rodrigo\\IdeaProjects\\BD2\\output\\bd.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            stream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void exportaArquivoTxt(List<Container> containers) {
        //Declarações
        byte[] tuplaByte, auxEntrada = new byte[3];
        int controle = 8, controleTupla = 0;
        int tamTupla,  tamEntrada = 0;
        int idContainer, idBloco;
        String text = "";
        String dados;

        for(Container container:containers) {
            // Retorna em um HashMap as colunas e seus tipo de dados
            HashMap<Integer, String> metaDados = headerTabela(container.controle);


            //Ler Blocos, e dentro dos blocos ler as tuplas e começar a salvar esses strings em uma grande string pra salvar no TXT
            for (Bloco bloco : container.blocos) {
                idContainer = bloco.dados[0];
                idBloco = Bloco.byteToInt(Bloco.getBytes(bloco.dados, 1, 3));

                while (controle < Bloco.byteToInt(Bloco.getBytes(bloco.dados, 5, 3))) {
                    text += String.valueOf(idContainer) + "|" + String.valueOf(idBloco) + "|";
                    // Calcula o espaço ocupado pela tupla no bloco
                    tamTupla = Bloco.byteToInt(Bloco.getBytes(bloco.dados, controle, 4)) + 2 * metaDados.size();

                    //Pega os dados da tupla
                    controle += 4;
                    tuplaByte = Bloco.getBytes(bloco.dados, controle, tamTupla);

                    for (int i = 0, j = 0; i < tamTupla + 1; i++, j++) {

                        if (i == tamEntrada + controleTupla) {

                            if (i != 0) {
                                //Teste
                                dados = new String(auxEntrada);
                                text += dados + "|";
                                System.out.println(dados);

                                if (i == tamTupla - 1 || i == tamTupla) break;
                                //Atualização do Controle
                                controleTupla += tamEntrada;
                                tamEntrada = Bloco.byte2ToInt(Bloco.getBytes(tuplaByte, controleTupla, 2));
                                auxEntrada = new byte[tamEntrada];
                                auxEntrada = new byte[3];
                                i = controleTupla - 1;
                                j = 0;
                            }

                            //Divide por cada dado na tuplaByte e salva em String
                            if (auxEntrada.length == 3) {
                                if (i < tamTupla - 1) {
                                    tamEntrada = Bloco.byte2ToInt(Bloco.getBytes(tuplaByte, controleTupla, 2));
                                    auxEntrada = new byte[tamEntrada];
                                    i = controleTupla + 1;
                                    j = -1;
                                    controleTupla += 2;
                                }
                            }
                        } else {
                            auxEntrada[j] = tuplaByte[i];
                        }


                    }
                    text += "\n";
                    auxEntrada = new byte[3];
                    controleTupla = 0;
                    tamEntrada = 0;
                    controle = controle + tamTupla;
                }

                controle = 8;
            }
        }

        //grava dados no arquivo
        try {
            PrintWriter out = new PrintWriter("C:\\Users\\Rodrigo\\IdeaProjects\\BD2\\output\\blocos.txt");
            out.println(text);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static HashMap<Integer, String> headerTabela(Bloco controle){
        HashMap<Integer, String> metaDadosAtrib = new HashMap<>();
        byte[] header;
        int index = 0;


        header = Bloco.getBytes(controle.dados, 11, Bloco.byte2ToInt(Bloco.getBytes(controle.dados, 9, 2)));
        String headerString = Bloco.byteToString(header);
        System.out.println(headerString);

        String[] metaDados = headerString.split("[|]");
        for (String string : metaDados) {
            string = string.replace("[", "#");
            String[] aux = string.split("#");

            String meta = aux[1];
            meta = meta.replace("]", "");
            metaDadosAtrib.put(index++, meta);
        }

        return metaDadosAtrib;
    }
}
