package q2p.asciiarter;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

// [X@%#x*+=-;:,.  ]
public class AsciiArter {
	/*
	TODO:
	add support to ansi symbols like solid walls
	scale intensity for truecolor
	invert colors for monochrome
	custom characters
	custom colors
	scale image full / semi / square
	scale char size in html
	copyright string
	image sequence(video)
	friendly jframe interface
	
	shape render maximize intensitivity
	shape render raw intensity
	
	on off background coloring
	
	coloring step
	
	gradient
	*/
	private static BufferedImage bimg;
	private static final File readDir =  new File("E:/@MyFolder/MEGA/p");
	private static final File writeDir =  new File("E:/@MyFolder/MEGA/p/ASCII");
	private static final String defaultTags = "hl sz8 sh scH tr hw16";
	private static String defaultColors = "000 007 00f 070 077 07f 0f0 0f7 0ff 700 707 70f 770 777 77f 7f0 7f7 7ff f00 f07 f0f f70 f77 f7f ff0 ff7 fff";
	//private static String defaultColors = "000 777 fff";
	private static String fileName = null;
	private static int tw = 0;
	private static int th = 0;
	private static boolean isHtml;
	private static boolean shapeRender;
	private static boolean useBackground = true; // TODO
	private static byte bunchSymbols; //BSNH
	private static short finalHeight;
	private static boolean useColor;
	private static int firH = 64;
	private static int firW;
	private static int fioY;
	private static int fis;
	private static int fip = 0;
	private static Font fiFont;
	private static short arH;
	private static short arW;
	private static short aoY;
	private static int as;
	private static int ap = 0;
	private static Font aFont;
	private static boolean[][][] aPixels;
	private static short[][][] fullColor;
	private static short[][] avilableColors;
	private static FileOutputStream fos;
	
	private static boolean needToScaleInensity = true; // TODO: custom command flag
	
	private static short[][] full = null;
	
	private static char[] avilableChars = " 0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-=+_!@#$%^&*()\"\':;[]{}<>.,?/\\~`|".toCharArray();
	//private static char[] avilableChars = " .=#0".toCharArray();
	private static short[] charIntensity = new short[avilableChars.length];
	
	public static void main(String[] args) {
		chooseFile();
		if(fileName == null) return;
		askForFlags();
		if(fileName == null) return;
		chooseHowToSave();
		if(fileName == null) return;
		selectFont();
		generateCharIntensityPreset(); // TODO: make unique
		buildFontPixels();
		transformToText();
		colorize();
		if(fileName == null) return;
		saveAsTextFile();
	}
	
	private static void colorize() {
		if(!useColor) return;
		String[] rs = defaultColors.split(" ");
		avilableColors = new short[rs.length][3];
		for(int k = 0; k < rs.length; k++) {
			for(byte i = 0; i < 3; i++) {
				for(byte j = 0; j < 3; j++) {
					avilableColors[k][j]=(short)(Integer.parseInt(rs[k].charAt(j)+"", 16)*16);
				}
			}
		}
		rs = null;
		fullColor = new short[th][tw][6];
		BufferedImage fi = new BufferedImage(arW,arH,BufferedImage.TYPE_INT_RGB);
		Graphics fig = fi.getGraphics();
		for(short y = 0; y < th; y++) {
			for(short x = 0; x < tw; x++) {
				fig.setColor(Color.WHITE);
				fig.fillRect(0, 0, arW, arH);
				fig.drawImage(bimg, 0, 0, arW, arH, x*arW, y*arH, (x+1)*arW, (y+1)*arH, null);
				int rf = 0;
				int gf = 0;
				int bf = 0;
				int df = 0;
				int rb = 0;
				int gb = 0;
				int bb = 0;
				int db = 0;
				for(byte fy = 0; fy < arH; fy++) {
					for(byte fx = 0; fx < arW; fx++) {
						//ins
						if(aPixels[full[y][x]][fy][fx]) {
							df++;
							int rgb = fi.getRGB(fx, fy);
							rf += (rgb >> 16) & 0x000000FF;
							gf += (rgb >>8 ) & 0x000000FF;
							bf += (rgb) & 0x000000FF;
						} else {
							db++;
							int rgb = fi.getRGB(fx, fy);
							rb += (rgb >> 16) & 0x000000FF;
							gb += (rgb >>8 ) & 0x000000FF;
							bb += (rgb) & 0x000000FF;
						}
					}
				}
				if(df!=0) {
					rf /= df;
					gf /= df;
					bf /= df;
				}
				if(db!=0) {
					rb /= db;
					gb /= db;
					bb /= db;
				}
				short[] c = avilableColors[0];
				int closestf = (c[0] - rf)*(c[0] - rf) + (c[1] - gf)*(c[1] - gf) + (c[2] - bf)*(c[2] - bf);
				int closestb = (c[0] - rb)*(c[0] - rb) + (c[1] - gb)*(c[1] - gb) + (c[2] - bb)*(c[2] - bb);
				int closestif = 0;
				int closestib = 0;
				for(int i = 1; i < avilableColors.length; i++) {
					c = avilableColors[i];
					int distf = (c[0] - rf)*(c[0] - rf) + (c[1] - gf)*(c[1] - gf) + (c[2] - bf)*(c[2] - bf);
					int distb = (c[0] - rb)*(c[0] - rb) + (c[1] - gb)*(c[1] - gb) + (c[2] - bb)*(c[2] - bb);
					if(distf < closestf) {
						closestf = distf;
						closestif = i;
					}
					if(distb < closestb) {
						closestb = distb;
						closestib = i;
					}
				}
				for(byte i = 0; i < 3; i++) {
					fullColor[y][x][i] = (short)avilableColors[closestif][i];
					fullColor[y][x][3+i] = (short)avilableColors[closestib][i];
				}
			}
		}
	}
	
	private static void buildFontPixels() {
		BufferedImage ci = new BufferedImage(arW,arH,BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D)ci.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF); // TODO:
		g2.setFont(aFont);
		aPixels = new boolean[avilableChars.length][arH][arW];
		for(short i = 0; i < avilableChars.length; i++) {
			g2.drawString(""+avilableChars[i], 0, aoY);
			for(byte y = 0; y < arH; y++) {
				for(byte x = 0; x < arW; x++) {
					int rgb = ci.getRGB(x, y);
					int g = (rgb >> 16) & 0x000000FF;
					aPixels[i][y][x] = Math.round(g/255)==1?true:false;
				}
			}
		}
	}

	private static void askForFlags() {
		boolean err = false;
		boolean customCharacters = false;
		boolean customColors = false;
		while(true) {
			if(err) JOptionPane.showMessageDialog(null, "You typed illegal flag sequense.", "Error", JOptionPane.ERROR_MESSAGE);
			String message = "Please type flags you want. Separate them using spaces.\n" +
					"hl  : (-tx) for styled html file\n" + // TODO: implement
					"tx  : (-hl) for plain text file\n" +
					"szA : height of a symbol in pixels\n" +
					"ct  : enable custom characters\n" + // TODO: show input menu
					"in  : (-sh) use intensity render\n" +
					"sh  : (-in) use shape render\n" + // TODO: implement
					"scA : scaling of final sequence ([B]unch, [S]tretch, [N]ormal, [H]ybrid)\n" +
					"hwA : (hl) height of a symbol on final html page\n" +
					"tr  : (hl) enable color\n" + // TODO: implement
					"cc  : (hl+tr) enable custom colors"; // TODO: show input menu
			String ret = JOptionPane.showInputDialog(null, message, defaultTags );
			if(ret == null) {
				fileName = null;
				return;
			}
			ret = ret.trim();
			if(!ret.contains(" ")) {
				err = true;
				continue;
			}
			String[] rs = ret.split(" ");
			boolean hl = false;
			boolean tx = false;
			short sz = -1;
			boolean ct = false;
			boolean in = false;
			boolean sh = false;
			byte sc = -1;
			short hw = -1;
			boolean tr = false;
			boolean cc = false;
			for(String r : rs) {
				r = r.trim();
				if(r.equals("hl")) hl = true;
				if(r.equals("tx")) tx = true;
				if(r.startsWith("sz")) sz = Short.parseShort(r.substring(2));
				if(r.equals("ct")) ct = true;
				if(r.equals("in")) in = true;
				if(r.equals("sh")) sh = true;
				if(r.startsWith("sc")) {
					switch(r.substring(2)){
					case "B":
						sc = 0;
						break;
					case "S":
						sc = 1;
						break;
					case "N":
						sc = 2;
						break;
					case "H":
						sc = 3;
						break;
					}
				}
				if(r.startsWith("hw")) hw = Short.parseShort(r.substring(2));
				if(r.equals("tr")) tr = true;
				if(r.equals("cc")) cc = true;
			}
			if((!hl && !tx) || (sz == -1) || (sc == -1) || (!sh && !in) || (cc && !tr) || (hw != -1 && !hl) || (tr && !hl)) {
				System.out.println("topKek");
				err = true;
				continue;
			}
			isHtml = hl;
			arH = sz;
			customCharacters = ct;
			shapeRender = sh;
			bunchSymbols = sc;
			if(isHtml && hw == -1) hw = arH;
			finalHeight = hw;
			useColor = tr;
			customColors = cc;
			break;
		}
		if(customCharacters) {
			while(true) {
				if(err) JOptionPane.showMessageDialog(null, "You typed no symbols.", "Error", JOptionPane.ERROR_MESSAGE);
				String ret = JOptionPane.showInputDialog(null, "Insert symbols you want to use", "").trim();
				if(ret.length() == 0) {
					err = true;
					continue;
				}
				avilableChars = ret.toCharArray();
			}
		}
		if(customColors) {
			while(true) {
				if(err) JOptionPane.showMessageDialog(null, "You typed illigal colors.", "Error", JOptionPane.ERROR_MESSAGE);
				String ret = JOptionPane.showInputDialog(null, "Insert colors you want to use.\nExample:\nfff 30a 070", defaultColors).trim();
				if(!ret.contains(" ")) {
					err = true;
					continue;
				}
				String[] rs = ret.split(" ");
				boolean gFine = true;
				avilableColors = new short[rs.length][3];
				for(int k = 0; k < rs.length; k++) {
					if(rs[k].length() != 3){
						err = true;
						continue;
					}
					for(byte i = 0; i < 3; i++) {
						boolean fine = false;
						for(char ctc : "0123456789abcdef".toCharArray()) {
							if(rs[k].charAt(i) == ctc) fine = true;
						}
						if(!fine) {
							gFine = false;
							break;
						}
					}
				}
				if(!gFine) {
					err = true;
					continue;
				}
				defaultColors = ret;
				// TODO: finilize
			}
		}
	}

	private static void chooseHowToSave() {
		JFileChooser fSaver = new JFileChooser();
		fSaver.setMultiSelectionEnabled(false);
		if(writeDir.exists() && writeDir.isDirectory()) fSaver.setCurrentDirectory(writeDir);
		fSaver.setSelectedFile(new File(fSaver.getCurrentDirectory().getAbsolutePath()+"/"+fileName+(isHtml ? ".html" : ".txt")));
		fileName = null;
		if (fSaver.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return;
		File file = fSaver.getSelectedFile();
		fSaver = null;
		if(file.exists()) {
			if(JOptionPane.showConfirmDialog(null, "Do you want to overwrite file "+file.getName()+" ?", "Overwrite file?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) return;
		} else {
			try {
				file.createNewFile();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Can not create file.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		int idx = file.getName().lastIndexOf(".");
		if(idx >= 0) {
			String fType = file.getName().substring(idx);
			if(fType.equalsIgnoreCase("html") || fType.equalsIgnoreCase("htm")) {
				isHtml = true;
			}
		}
		try {
			fos = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Can not write to file.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		fileName = file.getAbsolutePath();
	}

	private static void transformToText() {
		// TODO save as txt or html
		th = bimg.getHeight() / arH;
		System.out.println(arW);
		tw = bimg.getWidth() / arW;
		full = new short[th][tw];
		BufferedImage fi = new BufferedImage(arW,arH,BufferedImage.TYPE_INT_RGB);
		Graphics fig = fi.getGraphics();
		for(int y = 0; y < th; y++) {
			for(int x = 0; x < tw; x++) {
				fig.setColor(Color.WHITE);
				fig.fillRect(0, 0, arW, arH);
				fig.drawImage(bimg, 0, 0, arW, arH, x*arW, y*arH, (x+1)*arW, (y+1)*arH, null);
				
				if(!shapeRender) {
					int gr = 0;
					for(byte fx = 0; fx < arW; fx++) {
						for(byte fy = 0; fy < arH; fy++) {
							int rgb = fi.getRGB(fx, fy);
							int r = (rgb >> 16) & 0x000000FF;
							int g = (rgb >> 8 ) & 0x000000FF;
							int b = (rgb      ) & 0x000000FF;
							gr += r+g+b;
						}	
					}
					gr /= arW*arH*3;
					short mi = 255;
					
					for(short i : charIntensity) {
						mi = (short) Math.min(mi, Math.abs(gr-i));
					}
					for(short i = 0; i < charIntensity.length; i++) {
						if(mi != Math.abs(gr-charIntensity[i])) continue;
						full[y][x] = i;
					}
				} else {
					short fMap[][] = new short[arH][arW];
					int gr = 0;
					for(byte fy = 0; fy < arH; fy++) {
						for(byte fx = 0; fx < arW; fx++) {
							int rgb = fi.getRGB(fx, fy);
							int r = (rgb >> 16) & 0x000000FF;
							int g = (rgb >> 8 ) & 0x000000FF;
							int b = (rgb      ) & 0x000000FF;
							fMap[fy][fx] = (short)((r+g+b)/3);
						}	
					}
					short mi = fMap[0][0];
					for(byte fy = 0; fy < arH; fy++) {
						for(byte fx = 0; fx < arW; fx++) {
							mi = (short) Math.min(mi, fMap[fy][fx]);
						}
					}
					short ma = -1;
					for(byte fy = 0; fy < arH; fy++) {
						for(byte fx = 0; fx < arW; fx++) {
							fMap[fy][fx] -= mi;
							ma = (short) Math.max(ma, fMap[fy][fx]);
						}
					}
					float scl = 1;
					if(ma != 0) scl = (float)(255f / (float)ma);
					for(byte fy = 0; fy < arH; fy++) {
						for(byte fx = 0; fx < arW; fx++) {
							fMap[fy][fx] = (short)((float)fMap[fy][fx] * ma);
							fMap[fy][fx] = (short)(Math.round(fMap[fy][fx]/255f)>=1?1:0);
						}
					}
					short nmi = 0;
					short mz = -1;
					for(short sm = 0; sm < avilableChars.length; sm++) {
						short cz = 0;
						short co = 0;
						for(byte fy = 0; fy < arH; fy++) {
							for(byte fx = 0; fx < arW; fx++) {
								fMap[fy][fx] = (short)(Math.abs(fMap[fy][fx]-(aPixels[sm][fy][fx]?1:0)));
								if(fMap[fy][fx] == 0) cz++;
							}
						}
						if(useColor && useBackground)cz = (short) Math.max(cz, arH*arW-cz);
						if(cz > mz) {
							mz = cz;
							nmi = sm;
						}
					}
					full[y][x] = nmi;
				}
			}
		}
	}
	
	private static void selectFont() {
		String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		String cf = null;
		for(String f : fonts) {
			if (f.equals("Courier New")) cf = f;
			if (f.equals("Courier") && !cf.equals("Courier New")) cf = f;
			if (f.equals(Font.MONOSPACED) && !cf.equals("Courier New") && !cf.equals("Courier")) cf = f;
		}
		fonts = null;
		if(cf == null) {
			JOptionPane.showMessageDialog(null, "Can not find monospace font on your system.", "Error", JOptionPane.ERROR_MESSAGE);
			fileName = null;
			return;
		}
		
		switch(bunchSymbols) {
		case 0:
			fis = (int)(firH/0.6);
			break;
		case 1:
		case 2:
			fis = firH;
			break;
		case 3:
			fis = (int)(firH/0.8);
			break;
		}

		BufferedImage ci = new BufferedImage(firH,firH,BufferedImage.TYPE_INT_RGB);
		Graphics g = ci.getGraphics();
		FontMetrics fm;
		while(true) {
			Font font = new Font(cf, Font.PLAIN, fip); // TODO: bold switcher
			fm = g.getFontMetrics(font);
			if(fm.getHeight() < fis) {
				fip++;
			} else {
				break;
			}
		}
		
		fiFont = new Font(cf, Font.PLAIN , fip); // TODO: bold switcher
		fm = g.getFontMetrics(fiFont);
		switch(bunchSymbols) {
		case 0:
		case 1:
		case 3:
			firW = firH;
			break;
		case 2:
			firW = (int) fm.getStringBounds("=", g).getWidth();
			break;
		}
		fioY = fm.getAscent();
		
		switch(bunchSymbols) {
		case 0:
			as = (int)(arH/0.6);
			break;
		case 1:
		case 2:
			as = arH;
			break;
		case 3:
			as = (int)(arH/0.8);
			break;
		}

		ci = new BufferedImage(arH,arH,BufferedImage.TYPE_INT_RGB);
		g = ci.getGraphics();
		while(true) {
			Font font = new Font(cf, Font.PLAIN, ap); // TODO: bold switcher
			fm = g.getFontMetrics(font);
			if(fm.getHeight() < as) {
				ap++;
			} else {
				break;
			}
		}
		
		aFont = new Font(cf, Font.PLAIN , ap); // TODO: bold switcher
		fm = g.getFontMetrics(aFont);
		switch(bunchSymbols) {
		case 0:
		case 1:
		case 3:
			arW = arH;
			break;
		case 2:
			arW = (short)fm.getStringBounds("=", g).getWidth();
			break;
		}
		aoY = (short)fm.getAscent();
	}
	
	private static void generateCharIntensityPreset() {
		BufferedImage ci = new BufferedImage(firW,firH,BufferedImage.TYPE_INT_RGB);
		Graphics2D cig = (Graphics2D)ci.getGraphics();
		cig.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF); // TODO:
		cig.setFont(fiFont);
		FontMetrics fm = cig.getFontMetrics(fiFont);
		int gr;
		for(byte i = 0; i < avilableChars.length; i++) {
			cig.setColor(new Color(255,255,255));
			cig.fillRect(0, 0, firW, firH);
			cig.setColor(new Color(0,0,0));
			cig.drawString(""+avilableChars[i], 0, fioY);
			gr = 0;
			for(byte x = 0; x < firW; x++) {
				for(byte y = 0; y < firH; y++) {
					int rgb = ci.getRGB(x, y);
					int r = (rgb >> 16) & 0x000000FF;
					int g = (rgb >>8 ) & 0x000000FF;
					int b = (rgb) & 0x000000FF;
					gr += r+g+b;
				}
			}
			gr /= firW*firH*3;
			charIntensity[i] = (short) gr;
		}
		if(needToScaleInensity) { //TODO: intensity scale switcher
			short mi = 255;
			for(byte i = 0; i < charIntensity.length; i++) mi = (short) Math.min(charIntensity[i], mi);
			for(byte i = 0; i < charIntensity.length; i++) charIntensity[i] -= mi;
			short ma = 0;
			for(byte i = 0; i < charIntensity.length; i++) ma = (short) Math.max(charIntensity[i], ma);
			float scl = 255f/(float)ma;
			for(byte i = 0; i < charIntensity.length; i++) {
				charIntensity[i] *= scl;
				if(charIntensity[i] < 0) charIntensity[i] = 0;
				if(charIntensity[i] > 255) charIntensity[i] = 255;
			}
		}
		for(byte i = 0; i < avilableChars.length; i++) {
			System.out.println(avilableChars[i] + " - " + charIntensity[i]);
		}
	}

	private static void saveAsTextFile() {
		//TODO:
		try {
			System.out.println(fileName);
			if(!isHtml){
				for(int y = 0; y < th; y++) {
					for(int x = 0; x < tw; x++) fos.write(("" + full[y][x]).getBytes(StandardCharsets.UTF_8));
					if(y != th-1) fos.write("\n".getBytes(StandardCharsets.UTF_8));
				}
			} else {
				String str = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>";
				if(fileName.contains("/")) str += fileName.substring(fileName.lastIndexOf("/"));
				else str += fileName;
				str += "</title><style> * { padding: 0px; margin: 0px; font-family: 'Courier New', Courier, monospace; background-color: #777; } .ascii-art { display: inline-block; white-space: pre; font-size: "+finalHeight+"px;";
				//if(!useColor) str += "background-color: #ffffff; color: #000000;";// TODO:
				str += "background-color: #ffffff; color: #000000;";
				switch(bunchSymbols) {
				case 0:
					str+="letter-spacing: 0em; line-height: 0.6em;";
					break;
				case 1:
					str+="letter-spacing: 0.4em; line-height: 1em;";
					break;
				case 2:
					str+="letter-spacing: 0em; line-height: 1em;";
					break;
				case 3:
					str+="letter-spacing: 0.2em; line-height: 0.8em;";
					break;
				}
				str+="} p{display:inline-block;";
				switch(bunchSymbols) {
				case 0:
					str+="letter-spacing: 0em; line-height: 0.6em;";
					break;
				case 1:
					str+="letter-spacing: 0.4em; line-height: 1em;";
					break;
				case 2:
					str+="letter-spacing: 0em; line-height: 1em;";
					break;
				case 3:
					str+="letter-spacing: 0.2em; line-height: 0.8em;";
					break;
				}
				str+= "}</style></head><body><div class=\"ascii-art\">";
				fos.write(str.getBytes(StandardCharsets.UTF_8));
				//
				if(!useColor) {
					for(int y = 0; y < th; y++) {
						for(int x = 0; x < tw; x++) fos.write(("" + avilableChars[full[y][x]]).getBytes(StandardCharsets.UTF_8));
						if(y != th-1) fos.write("\n".getBytes(StandardCharsets.UTF_8));
					}
				} else {
					for(int y = 0; y < th; y++) {
						for(int x = 0; x < tw; x++) fos.write(("<p style=\""+getColor(fullColor[y][x])+"\">" + avilableChars[full[y][x]]+"</p>").getBytes(StandardCharsets.UTF_8));
						if(y != th-1) fos.write("\n".getBytes(StandardCharsets.UTF_8));
					}
				}
				//
				str = "</div></body></html>";
				fos.write(str.getBytes(StandardCharsets.UTF_8));
			}
			fos.flush();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Can not write to file.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			fos.close();
		} catch (IOException e) {
			System.exit(1); // FATAL ERROR
		}
		fos = null;
	}
	
	private static String getColor(short c[]) {
		String ret = "color:#";
		for(byte i = 0; i < 3; i++) ret += Integer.toHexString(c[i]/16);
		ret += ";";
		if(!useBackground){
			ret += "background-color:#fff;";
			return ret;
		}
		ret += "background-color:#";
		for(byte i = 3; i < 6; i++) ret += Integer.toHexString(c[i]/16);
		ret += ";";
		return ret;
	}
	
	private static void chooseFile() {
		boolean err = false;
		JFileChooser fChooser = new JFileChooser();
		fChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fChooser.setMultiSelectionEnabled(false);
		if(readDir.exists() && readDir.isDirectory()) fChooser.setCurrentDirectory(readDir);
		File file = null;
		do {
			if (fChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;
			file = fChooser.getSelectedFile();
			if(!file.exists()) {
				JOptionPane.showMessageDialog(null, "File not found.", "Error", JOptionPane.ERROR_MESSAGE);
				err = true;
			}
			try {
				BufferedImage iimg = ImageIO.read(file);
				if (iimg == null) {
					JOptionPane.showMessageDialog(null, "File must be a image.", "Error", JOptionPane.ERROR_MESSAGE);
					err = true;
				}
				bimg = new BufferedImage(iimg.getWidth(), iimg.getHeight(), BufferedImage.TYPE_INT_RGB);
				bimg.getGraphics().setColor(new Color(255,255,255));
				bimg.getGraphics().fillRect(0, 0, bimg.getWidth(), bimg.getHeight());
				bimg.getGraphics().drawImage(iimg, 0, 0, bimg.getWidth(), bimg.getHeight(), 0, 0, iimg.getWidth(), iimg.getHeight(), null);
				iimg = null;
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Can not read the file.", "Error", JOptionPane.ERROR_MESSAGE);
				err = true;
			}
		} while(err);
		fileName = file.getName();
	}
}