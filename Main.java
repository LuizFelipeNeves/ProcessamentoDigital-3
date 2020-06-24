// Pacote Generico
import java.io.File;
import java.io.IOException;

// Pacotes do AWT
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.GridLayout;

// Pacote para interface grafica
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;

// Pacotes uteis
import java.util.Arrays;

/*
Nome: Luiz Felipe das Neves
RA: 170001569
UNISAL - Lorena Computação Gráfica.
*/

// Leitura em mode Imediado
import javax.imageio.ImageIO;

public class Main extends JFrame {
    public static void main(String[] args) {
        // Nome da Imagem
        String aFile = "./images/DWMTM.png";

        // Tipo de Interpolador a ser usuado
        String tipo_filtro = "Oeste"; // DWMTM, Uniforme, Gauss, PassaBaixa, Norte, Sul, Leste, Oeste

        // Filtro DWMTM
        // int nc1 = 0, nc2 = 0;

        // Ruivo Uniforme
        int nc1 = 50, nc2 = 45;

        // Ruivo Gauss
        //int nc1 = 0, nc2 = 10;

        // PassaBaixa
        // int nc1 = 4, nc2 = 0;

        // Verifica as Entradas
        checkInputs(tipo_filtro, nc1, nc2);

        JFrame.setDefaultLookAndFeelDecorated(true);

        JFrame tal = new Main(aFile, tipo_filtro, nc1, nc2);
        tal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tal.setVisible(true);
    }

    private Main(String aFile, String tipo_filtro, int nc1, int nc2) {
        // Inicia as variaveis que serao usadas.
        BufferedImage bimage = null, dest = null;
        JLabel img1, img2;
        int w, h;

        // Faz a leitura da imagem
        File arq = new File(aFile);
        try {
            bimage = ImageIO.read(arq);
        } catch (IOException ex) {
            System.out.println("Arquivo não existe!");
            System.exit(0);
        }

        // Processa a imagem com base no tipo de filtro e entradas.
        dest = ApplyFiltro(bimage, tipo_filtro, nc1, nc2);

        // Monta um painel com as duas imagems antes/depois.
        setTitle("Aplicação de filtro/Ruivo: " + tipo_filtro);
        getContentPane().setLayout(new GridLayout(1, 2));

        img1 = new JLabel(new ImageIcon(bimage));
        img2 = new JLabel(new ImageIcon(dest));
        getContentPane().add(new JScrollPane(img1));
        getContentPane().add(new JScrollPane(img2));

        w = dest.getWidth();
        h = dest.getHeight();
        setSize(w, h / 2);

        try {
            ImageIO.write(dest, "png", new File("./images" + tipo_filtro + ".png"));
        } catch (IOException e) {
            System.out.println("Problema ao gravar o arquivo");
            System.exit(0);
        }


    }

    public static void checkInputs(String tipo_filtro, int nc1, int nc2) {
        switch (tipo_filtro) {
            case "DWMTM":
                if (nc1 < 0 || nc2 < 0) {
                    System.out.println("Valor de 'nc1' e 'nc2' deve ser positivo");
                    System.exit(0);
                }
                break;
            case "Uniforme":
                if (nc1 < 0 || nc2 < 0) {
                    System.out.println("valores de 'nc1' e 'nc2' devem ser positivos");
                    System.exit(0);
                }

                if (nc1 == nc2) {
                    System.out.println("Valores de 'nc1' e 'nc2' devem ser diferentes");
                    System.exit(0);
                }
                break;
            case "Gauss":
                if (nc1 < 0 || nc2 < 0) {
                    System.out.println("valores de 'nc1' e 'nc2' devem ser positivos");
                    System.exit(0);
                }

                if (nc1 == nc2) {
                    System.out.println("Valores de 'nc1' e 'nc2' devem ser diferentes");
                    System.exit(0);
                }
                break;
            case "PassaBaixa":
                if (nc1 <= 0 && nc1 > 4) {
                    System.out.println("Valores validos de 'nc1' >= 1 && <= 4" + nc1);
                    System.exit(0);
                }
                break;
            case "Norte":
                break;
            case "Sul":
                break;
            case "Leste":
                break;
            case "Oeste":
                break;
            default:
                System.out.println("Tipo invalido, use:");
                System.out.println("\tDWMTM, Uniforme, Gauss, PassaBaixa");
                System.exit(0);
        }
    }

    protected BufferedImage ApplyFiltro(BufferedImage src, String tipo_filtro, int nc1, int nc2) {

        // Com base no ID da operação, executa as função de rotação para o tipo escolhido.
        switch (tipo_filtro) {
            case "DWMTM":
                return FiltroDWMTM(src, nc1, nc2);
            case "Uniforme":
                return RuidoUniforme(src, nc1, nc2);
            case "Gauss":
                return RuidoGauss(src, nc1, nc2);
            case "PassaBaixa":
                return FiltroPassaBaixa(src, nc1);
            case "Norte":
                return Direcional(src, tipo_filtro);
            case "Sul":
                return Direcional(src, tipo_filtro);
            case "Leste":
                return Direcional(src, tipo_filtro);
            case "Oeste":
                return Direcional(src, tipo_filtro);
            default:
                JOptionPane.showMessageDialog(null, "Digite números entre 1 e 4");
                System.exit(0);
                return null;
        }
    }


    public BufferedImage FiltroDWMTM(BufferedImage src, int nc1, int nc2) {
        int mediana[], med, tmp, sum, gray, i, j, X, Y, x1, y1;

        mediana = new int[9];

        // Define o ponto ancora (Ponto de Inicio)
        int w = src.getWidth();
        int h = src.getHeight();

        // Raster
        Raster srcR = src.getRaster();

        int tipo = BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage outImage = new BufferedImage(w, h, tipo);
        WritableRaster WR = outImage.getRaster();

        Raster outR = outImage.getRaster();

        for (Y = 2; Y < h - 2; Y++)
            for (X = 2; X < w - 2; X++) {
                // Le os 9 pixels para a matriz mediana
                tmp = 0;
                for (y1 = -1; y1 < 2; y1++)
                    for (x1 = -1; x1 < 2; x1++) {
                        mediana[tmp] = srcR.getSample(X + x1, Y + y1, 0);
                        tmp++;
                    }
                Arrays.sort(mediana);
                med = mediana[4];
                sum = 0;
                tmp = 0;
                for (y1 = -2; y1 < 3; y1++)
                    for (x1 = -2; x1 < 3; x1++) {
                        gray = srcR.getSample(X + x1, Y + y1, 0);
                        if (gray >= (med - nc1 * nc2))
                            if (gray <= (med + nc1 * nc2)) {
                                sum = sum + gray;
                                tmp++;
                            }
                    }
                WR.setSample(X, Y, 0, sum / tmp);
            }
        return outImage;
    }

    public BufferedImage RuidoUniforme(BufferedImage src, int nc1, int nc2) {
        double range, randomBright;
        int nc, v;

        // Define o ponto ancora (Ponto de Inicio)
        int w = src.getWidth();
        int h = src.getHeight();

        // Raster
        Raster srcR = src.getRaster();

        int tipo = BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage outImage = new BufferedImage(w, h, tipo);
        WritableRaster WR = outImage.getRaster();

        Raster outR = outImage.getRaster();

        if (nc1 > nc2) {
            int tmp = nc2;
            nc2 = nc1;
            nc1 = tmp;
        }
        range = nc2 - nc1;

        for (int Y = 0; Y < h; Y++)
            for (int X = 0; X < w; X++) {
                nc = srcR.getSample(X, Y, 0);
                WR.setSample(X, Y, 0, nc);
            }

        for (int Y = 0; Y < h; Y++)
            for (int X = 0; X < w; X++) {
                randomBright = Math.random() * range;
                nc = outR.getSample(X, Y, 0);
                v = (nc & 0xff) + (int) randomBright;

                if (v < 0)
                    v = 0;
                if (v > 255)
                    v = 255;
                WR.setSample(X, Y, 0, v);
            }
        return outImage;
    }

    public BufferedImage RuidoGauss(BufferedImage src, int nc1, int nc2) {

        double range, randonBright;
        int w, h, nc, tipo;

        h = src.getHeight();
        w = src.getWidth();

        tipo = BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage outImage = new BufferedImage(w, h, tipo);
        WritableRaster outWR = outImage.getRaster();
        Raster srcR = src.getRaster();
        Raster outR = outImage.getRaster();

        if (nc1 > nc2) {
            int tmp = nc2;
            nc2 = nc1;
            nc1 = tmp;
        }

        range = nc2 - nc1;

        src.copyData(outWR);

        for (int Y = 0; Y < h; Y++)
            for (int X = 0; X < w; X++) {
                randonBright = Math.random() * range;
                nc = outR.getSample(X, Y, 0);
                outWR.setSample(X, Y, 0, nc + (byte) randonBright);
            }
        return outImage;
    }

    public BufferedImage FiltroPassaBaixa(BufferedImage src, int nc1) {
        float matrizMedia[] = new float[9];

        switch (nc1) {
            case 1:
                matrizMedia[0] = 1/10f;
                matrizMedia[1] = 1/10f;
                matrizMedia[2] = 1/10f;
                matrizMedia[3] = 1/10f;
                matrizMedia[4] = 2/10f;
                matrizMedia[5] = 1/10f;
                matrizMedia[6] = 1/10f;
                matrizMedia[7] = 1/10f;
                matrizMedia[8] = 1/10f;
                break;
            case 2:
                matrizMedia[0] = 1/12f;
                matrizMedia[1] = 1/12f;
                matrizMedia[2] = 1/12f;
                matrizMedia[3] = 1/12f;
                matrizMedia[4] = 4/12f;
                matrizMedia[5] = 1/12f;
                matrizMedia[6] = 1/12f;
                matrizMedia[7] = 1/12f;
                matrizMedia[8] = 1/12f;
                break;
            case 3:
                matrizMedia[0] = 1/20f;
                matrizMedia[1] = 1/20f;
                matrizMedia[2] = 1/20f;
                matrizMedia[3] = 1/20f;
                matrizMedia[4] = 12/20f;
                matrizMedia[5] = 1/20f;
                matrizMedia[6] = 1/20f;
                matrizMedia[7] = 1/20f;
                matrizMedia[8] = 1/20f;
                break;
            case 4:
                matrizMedia[0] = 1/9f;
                matrizMedia[1] = 1/9f;
                matrizMedia[2] = 1/9f;
                matrizMedia[3] = 1/9f;
                matrizMedia[4] = 1/9f;
                matrizMedia[5] = 1/9f;
                matrizMedia[6] = 1/9f;
                matrizMedia[7] = 1/9f;
                matrizMedia[8] = 1/9f;
                break;
        }

        Kernel filtroMedia = new Kernel(3, 3, matrizMedia);

        Raster srcR = src.getRaster();
        ConvolveOp blur = new ConvolveOp(filtroMedia);
        WritableRaster WR = blur.filter(srcR, null);

        BufferedImage outImage = new BufferedImage(src.getColorModel(), WR, false, null);

        return outImage;
    }


    public BufferedImage Direcional(BufferedImage src, String direcao) {
        float[] matriz = new float[9];

        switch (direcao) {
            case "Norte":
                matriz = new float[] {
                    1f, 1f, 1f,
                    1f, -2f, 1f, -1f, -1f, -1f
                };
                break;
            case "Sul":
                matriz = new float[] {
                    -1f, -1f, -1f,
                    1f, -2f, 1f,
                    1f, 1f, 1f
                };
                break;
            case "Leste":
                matriz = new float[] {
                    -1f, 1f, 1f, -1f, -2f, 1f, -1f, 1f, 1f
                };
                break;
            case "Oeste":
                matriz = new float[] {
                    1f, 1f, -1f,
                    1f, -2f, -1f,
                    1f, 1f, -1f
                };
                break;
            default:
                JOptionPane.showMessageDialog(null, "Tente Direcao: Norte, Sul, Leste, Oeste");
                System.exit(0);
                return null;
        }

        Raster srcR = src.getRaster();
        Kernel filtroDirecao = new Kernel(3, 3, matriz);
        ConvolveOp direcaoConv = new ConvolveOp(filtroDirecao);
        WritableRaster WR = direcaoConv.filter(srcR, null);
        BufferedImage outImage = new BufferedImage(src.getColorModel(), WR, false, null);

        return outImage;
    }
}