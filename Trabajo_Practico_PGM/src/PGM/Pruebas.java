package PGM;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

public class Pruebas {

	public static void main(String[] args) {
		int[][] imagen = importacion_PGM("ejemplo_p2.pgm");

		for (int i = 0; i < imagen.length; i++) {
			for (int j = 0; j < imagen[i].length; j++) {
				System.out.print(imagen[i][j] + " ");
			}
			System.out.println();
		}

	}

	// Clase FileInputStream permite leer bytes desde un archivo
	// Clase Scanner facileta la lectura de texto

	public static int[][] importacion_PGM(String filePath) {
		try {
		int columnas, filas; // Width=columnas | Height=filas
		int valor_max, lineas_leidas=3;

		FileInputStream fileInputStream = new FileInputStream(filePath);
		Scanner scanner = new Scanner(fileInputStream);

		// P2 ASCII | P5 binario
		String magic = scanner.nextLine(); // Leemos el magic number
		
		String line = scanner.nextLine(); // Sacamos los comentarios
		while (line.startsWith("#")) {
			line = scanner.nextLine();
			lineas_leidas++;

		}
		
		//La ultima linea leida tiene la altura o la altura y el largo.
		Scanner lineScanner = new Scanner(line);
		columnas = lineScanner.nextInt();
		if(lineScanner.hasNextInt())
		{
			filas = lineScanner.nextInt();		
		}
		else
		{
			lineScanner.close();
			line = scanner.nextLine();
			lineScanner = new Scanner(line);
			filas = lineScanner.nextInt();	
			lineas_leidas++;
		}
		lineScanner.close();

		valor_max = scanner.nextInt();
		
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
		
		}
		catch(IOException e)
		{
			return null;
		}
	}
	
	private static int[][] importacion_PGM_binaria(String filePath, int columnas, int filas, int lineas_leidas)
	{
		try
		{
		FileInputStream fileInputStream = new FileInputStream(filePath);
		DataInputStream dis = new DataInputStream(fileInputStream);
		 
		 // Descarto las primeras n lineas leídas del encabezado
		 int numnewlines = lineas_leidas;
		 while (numnewlines > 0) {
		     char c;
		     do {
		         c = (char)(dis.readUnsignedByte());
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
		}
		catch(IOException e)
		{
			return null;
		}
	}
}
