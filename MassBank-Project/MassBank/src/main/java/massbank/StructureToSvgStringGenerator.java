package massbank;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;

public class StructureToSvgStringGenerator {
	
	public static String fromInChI(String inchi){
		
		String svg	= null;
		try {
			// get atom container
			InChIGeneratorFactory inchiFactory = InChIGeneratorFactory.getInstance();
			InChIToStructure inchi2structure = inchiFactory.getInChIToStructure(inchi, DefaultChemObjectBuilder.getInstance());
			IAtomContainer mol	= inchi2structure.getAtomContainer();
			
			// get the SVG XML string
			svg = new DepictionGenerator().depict(mol).toSvgStr();
		} catch (CDKException e) {
			System.out.println("Warning: " + e.getLocalizedMessage());
		}
		
		return svg;
	}
	public static String fromSMILES(String smiles){
		
		String svg	= null;
		try {
			// get atom container
	    	SmilesParser smipar = new SmilesParser(DefaultChemObjectBuilder.getInstance());
	    	IAtomContainer mol = smipar.parseSmiles(smiles);
			
			// get the SVG XML string
			svg = new DepictionGenerator().depict(mol).toSvgStr();
		} catch (CDKException e) {
			System.out.println("Warning: " + e.getLocalizedMessage());
		}
		
		return svg;
	}
	public static String resizeSvg(String svg, int width, int heigth){
		return svg.replaceAll(
				"width='[0-9]*(\\.[0-9]*)?mm' height='[0-9]*(\\.[0-9]*)?mm'", 
				"width=\"" + width + "\" height=\"" + heigth + "\""
		);
	}
	public static String setSvgStyle(String svg, String styleParameters){
		return svg.replaceAll(
				"viewBox", 
				"style=\"" + styleParameters + "\" viewBox"
		);
	}
	public static ClickablePreviewImageData createClickablePreviewImage(
			String databaseName, String accession, String tmpFileFolder, String tmpUrlFolder,
			int sizeSmall, int sizeMedium, int sizeBig
	){
		// fetch accession data
//		DatabaseManager dbManager	= new DatabaseManager(databaseName);
//		AccessionData accData	= dbManager.getAccessionData(accession);
//		dbManager.closeConnection();
		
		AccessionData accData	= AccessionData.getAccessionDataFromFile(databaseName, accession);
		if(accData == null)
			return null;
		
		return createClickablePreviewImage(
				accData, tmpFileFolder, tmpUrlFolder,
				sizeSmall, sizeMedium, sizeBig
		);
	}
	public static ClickablePreviewImageData createClickablePreviewImage(
			AccessionData accData, String tmpFileFolder, String tmpUrlFolder,
			int sizeSmall, int sizeMedium, int sizeBig
	){
		// fetch structure
		String accession	= accData.ACCESSION;
		String inchi		= accData.CH$IUPAC;
		String smiles		= accData.CH$SMILES;
		
		if(inchi != null && (inchi.equals("NA") || inchi.equals("N/A")))
			inchi	= null;
		if(smiles != null && (smiles.equals("NA") || smiles.equals("N/A")))
			smiles	= null;
		
		// generate SVG string from structure (inchi / smiles)
		boolean inchiThere	= inchi  != null;
		boolean smilesThere	= smiles != null;
		
		String svg = null;
		// structure there --> generate svg string
		if(svg == null && smilesThere)	svg	= StructureToSvgStringGenerator.fromSMILES(smiles);
		if(svg == null && inchiThere)	svg	= StructureToSvgStringGenerator.fromInChI(inchi);
		
		// display svg
		if(svg != null){
			// path to temp file as local file and as url
			
			// file names
			final SimpleDateFormat sdf	= new SimpleDateFormat("yyMMdd_HHmmss_SSS");
			String accession2			= accession.replaceAll("[^0-9a-zA-Z]", "_");
			
			String fileNameSmall		= sdf.format(new Date()) + "_" + accession2 + "_small.svg";
			String fileNameMedium		= sdf.format(new Date()) + "_" + accession2 + "_medium.svg";
			String fileNameBig			= sdf.format(new Date()) + "_" + accession2 + "_big.svg";
			
			String tmpFileSmall			= (new File(tmpFileFolder + fileNameSmall	)).getPath();
			String tmpFileMedium		= (new File(tmpFileFolder + fileNameMedium	)).getPath();
			String tmpFileBig			= (new File(tmpFileFolder + fileNameBig		)).getPath();
			
			String tmpUrlSmall			= tmpUrlFolder + "/" + fileNameSmall;
			String tmpUrlMedium			= tmpUrlFolder + "/" + fileNameMedium;
			String tmpUrlBig			= tmpUrlFolder + "/" + fileNameBig;
			
			// adapt size of svg image
			String svgSmall		= StructureToSvgStringGenerator.resizeSvg(svg, sizeSmall,	sizeSmall);
			String svgMedium	= StructureToSvgStringGenerator.resizeSvg(svg, sizeMedium,	sizeMedium);
			String svgBig		= StructureToSvgStringGenerator.resizeSvg(svg, sizeBig,		sizeBig);
			
			return new ClickablePreviewImageData(
					tmpFileSmall,
					tmpFileMedium,
					tmpFileBig, 
					tmpUrlSmall, 
					tmpUrlMedium, 
					tmpUrlBig,
					svgSmall, 
					svgMedium,
					svgBig            
			);
		} else
			return null;
	}
	public static class ClickablePreviewImageData{
		public final String tmpFileSmall;
		public final String tmpFileMedium;
		public final String tmpFileBig;
		public final String tmpUrlSmall;
		public final String tmpUrlMedium;
		public final String tmpUrlBig;
		
		public final String svgSmall;
		public final String svgMedium;
		public final String svgBig;
		
		public ClickablePreviewImageData(
				String tmpFileSmall,
				String tmpFileMedium,
				String tmpFileBig,
				String tmpUrlSmall,
				String tmpUrlMedium,
				String tmpUrlBig,
				String svgSmall,
				String svgMedium,
				String svgBig
		) {
			this.tmpFileSmall	= tmpFileSmall;
			this.tmpFileMedium	= tmpFileMedium;
			this.tmpFileBig		= tmpFileBig;   
			this.tmpUrlSmall	= tmpUrlSmall;
			this.tmpUrlMedium	= tmpUrlMedium;
			this.tmpUrlBig		= tmpUrlBig;
			this.svgSmall		= svgSmall;         
			this.svgMedium		= svgMedium;        
			this.svgBig			= svgBig;           
		}
		public static void writeToFile(String svg, String file){
			try {
				PrintWriter pw = new PrintWriter(file);
				pw.println(svg);
				pw.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}