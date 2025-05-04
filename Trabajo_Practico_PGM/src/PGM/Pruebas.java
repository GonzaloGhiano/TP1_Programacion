package PGM;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import java.io.FileWriter;

public class Pruebas {

	public static void main(String[] args) {
		int[][] imagen = importacion_PGM("image_p2.pgm");

		for (int i = 0; i < imagen.length; i++) {
			for (int j = 0; j < imagen[i].length; j++) {
				System.out.print(imagen[i][j] + " ");
			}
			System.out.println();
		}

		int[][] umbralizado = umbralizacion_promedio_de_imagen("image_p2.pgm");
		System.out.println("PROMEDIO");
		for (int i = 0; i < umbralizado.length; i++) {
			for (int j = 0; j < umbralizado[i].length; j++) {
				System.out.print(umbralizado[i][j] + " ");
			}
			System.out.println();
		}
		exportacion_PGM(umbralizado, 
			    "C:\\Users\\Gonza\\Desktop\\TP_PGM\\PROMEDIO.pgm");
		
		umbralizado = umbralizacion_ISODATA("image_p2.pgm");
		System.out.println("ISODATA");
		for (int i = 0; i < umbralizado.length; i++) {
			for (int j = 0; j < umbralizado[i].length; j++) {
				System.out.print(umbralizado[i][j] + " ");
			}
			System.out.println();
		}
		
		exportacion_PGM(umbralizado, 
			    "C:\\Users\\Gonza\\Desktop\\TP_PGM\\ISODATA.pgm");


////////////////////////////////////////////
		
		umbralizado= umbralizacion_valor_fijo("image_p2.pgm", 60);
		exportacion_PGM(umbralizado, 
			    "C:\\Users\\Gonza\\Desktop\\TP_PGM\\VALORFIJO.pgm");
	}

	// Clase FileInputStream permite leer bytes desde un archivo
	// Clase Scanner facileta la lectura de texto

	public static int[][] importacion_PGM(String filePath) {
		try {
			int columnas, filas; // Width=columnas | Height=filas
			int valor_max, lineas_leidas = 3;

			FileInputStream fileInputStream = new FileInputStream(filePath);
			Scanner scanner = new Scanner(fileInputStream);

			// P2 ASCII | P5 binario
			String magic = scanner.nextLine(); // Leemos el magic number

			String line = scanner.nextLine(); // Sacamos los comentarios
			while (line.startsWith("#")) {
				line = scanner.nextLine();
				lineas_leidas++;

			}

			// La ultima linea leida tiene la altura o la altura y el largo.
			Scanner lineScanner = new Scanner(line);
			columnas = lineScanner.nextInt();
			if (lineScanner.hasNextInt()) {
				filas = lineScanner.nextInt();
			} else {
				lineScanner.close();
				line = scanner.nextLine();
				lineScanner = new Scanner(line);
				filas = lineScanner.nextInt();
				lineas_leidas++;
			}
			lineScanner.close();

			valor_max = scanner.nextInt();

			System.out.println("Filas: " + filas);
			System.out.println("Columnas: " + columnas);

			int[][] im = new int[filas][columnas];

			if ("P2".equals(magic))
				for (int i = 0; i < filas; i++) {
					for (int j = 0; j < columnas; j++) {
						im[i][j] = scanner.nextInt();
					}

				}
			else if ("P5".equals(magic)) {
				scanner.close();
				fileInputStream.close();
				return importacion_PGM_binaria(filePath, columnas, filas, lineas_leidas);
			}
			scanner.close();
			fileInputStream.close();
			return im;

		} catch (IOException e) {
			return null;
		}
	}

	private static int[][] importacion_PGM_binaria(String filePath, int columnas, int filas, int lineas_leidas) {
		try {
			FileInputStream fileInputStream = new FileInputStream(filePath);
			DataInputStream dis = new DataInputStream(fileInputStream);

			// Descarto las primeras n lineas leídas del encabezado
			int numnewlines = lineas_leidas;
			while (numnewlines > 0) {
				char c;
				do {
					c = (char) (dis.readUnsignedByte());
				} while (c != '\n');
				numnewlines--;
			}

			// read the image data
			int[][] data2D = new int[filas][columnas];
			for (int row = 0; row < filas; row++) {
				for (int col = 0; col < columnas; col++) {
					data2D[row][col] = dis.readUnsignedByte();
				}
			}

			dis.close();
			fileInputStream.close();

			return data2D;
		} catch (IOException e) {
			return null;
		}
	}

	public static int[][] umbralizacion_promedio_de_imagen(String filePath) {
		int[][] imagen = importacion_PGM(filePath);
		double promedio = 0;
		int largo = imagen.length, ancho = imagen[0].length;

		for (int i = 0; i < imagen.length; i++) {
			for (int j = 0; j < imagen[i].length; j++)
				promedio += imagen[i][j];
		}
		promedio = promedio / ((double) (largo * ancho));

		// Matriz resultado
		int[][] im_umbralizada = new int[largo][ancho];

		// Comparamos cada pixel, si pixel>=umbral -> pixel es blanco
		for (int i = 0; i < imagen.length; i++) {
			for (int j = 0; j < imagen[i].length; j++)
				im_umbralizada[i][j] = (imagen[i][j] < promedio) ? 0 : 255;
		}
		return im_umbralizada;
	}

	public static int[][] umbralizacion_ISODATA(String filePath) {
		int[][] imagen = importacion_PGM(filePath);
		int largo = imagen.length, ancho = imagen[0].length;

		int min = 255, max = 0;
		// Paso 1: obtener min y max de la matriz
		for (int i = 0; i < imagen.length; i++) {
			for (int j = 0; j < imagen[i].length; j++) {
				if (imagen[i][j] < min)
					min = imagen[i][j];
				if (imagen[i][j] > max)
					max = imagen[i][j];
			}
		}

		// Calcular umbral ponderando min y max.
		int umbralAnterior = -1;
		int umbral = (min + max) / 2;

		// Paso 2: iterar hasta que el umbral se estabilice
		while (umbral != umbralAnterior) {
			int suma_oscuros = 0, suma_claros = 0;
			int contador_oscuros = 0, contador_claros = 0;

			for (int i = 0; i < imagen.length; i++) {
				for (int j = 0; j < imagen[i].length; j++) {
					if (imagen[i][j] < umbral) {
						suma_oscuros += imagen[i][j];
						contador_oscuros++;
					} else {
						suma_claros += imagen[i][j];
						contador_claros++;
					}
				}
			}
			int media_oscuros = 
					(contador_oscuros == 0) ? 0 : (suma_oscuros / contador_oscuros);
			int media_claros = 
					(contador_claros == 0) ? 0 : (suma_claros / contador_claros);
			umbralAnterior = umbral;
			umbral = (media_oscuros + media_claros) / 2;
		}

		// Matriz resultado
		int[][] im_umbralizada = new int[largo][ancho];

		// Comparamos cada pixel, si es mayor o igual al umbral, el pixel es blanco
		for (int i = 0; i < imagen.length; i++) {
			for (int j = 0; j < imagen[i].length; j++)
				im_umbralizada[i][j] = (imagen[i][j] < umbral) ? 0 : 255;
		}

		return im_umbralizada;
	}

	public static void exportacion_PGM(int[][] matriz, String filePath) {
		int alto = matriz.length;
		int ancho = matriz[0].length;

		// FileWrite -> Con nombre lo deja en src, también funciona con Path Completo
		try (FileWriter writer = new FileWriter(filePath)) {
			writer.write("P2\n");
			writer.write("# Imagen exportada desde Java\n");
			writer.write(ancho + " " + alto + "\n");
			writer.write(255 + "\n");

			for (int i = 0; i < alto; i++) {
				for (int j = 0; j < ancho; j++) {
					writer.write(matriz[i][j] + " ");
				}
				writer.write("\n");
			}
			
		writer.close();
		
		}catch (IOException e) {
			return;
		}
	}
	
	
	public static int[][] umbralizacion_valor_fijo(String filePath, int umbral) {
		int[][] imagen = importacion_PGM(filePath);
		int largo = imagen.length, ancho = imagen[0].length;

		// Matriz resultado
		int[][] im_umbralizada = new int[largo][ancho];

		// Comparamos cada pixel, si es mayor o igual al umbral, el pixel es blanco
		for (int i = 0; i < imagen.length; i++) {
			for (int j = 0; j < imagen[i].length; j++)
				im_umbralizada[i][j] = (imagen[i][j] < umbral) ? 0 : 255;
		}

		return im_umbralizada;
	}

}