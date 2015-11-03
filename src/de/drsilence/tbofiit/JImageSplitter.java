package de.drsilence.tbofiit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import de.drsilence.utils.data.ImageManager;
import de.drsilence.utils.swing.FlowLayout;



public class JImageSplitter extends JFrame {
	
	private ImageManager imageManager = new ImageManager();
	
	public static BufferedImage copyImage(BufferedImage source){
	    BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
	    Graphics g = b.getGraphics();
	    g.drawImage(source, 0, 0, null);
	    g.dispose();
	    return b;
	}
	
	public JImageSplitter() {
		super("Image Splitter for Isaac multi-item Images");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(800, 400);
		Container contentPane = this.getContentPane();
		
//		BufferedImage origImg = this.imageManager.getBufferedImage("data/afterbirth-items.png");
		BufferedImage origImg = this.imageManager.getBufferedImage("data/rebirth-items.png");

// Original Image & show:		
		JPanel scrollpaneView = new JPanel( new FlowLayout( 1 ) );
//		scrollpaneView.setLayout( new BoxLayout(scrollpaneView, BoxLayout.PAGE_AXIS));
		JScrollPane scrollPane = new JScrollPane( scrollpaneView );
		JLabel origImgLabel = new JLabel( "Original: " );
		origImgLabel.setIcon( new ImageIcon( origImg ) );
		origImgLabel.setHorizontalTextPosition( JLabel.LEFT );
		scrollpaneView.add( origImgLabel );
		contentPane.add( scrollPane , BorderLayout.CENTER );
		
// Finding Splits:
		int w   = origImg.getWidth();
		int h   = origImg.getHeight();
		int c   = origImg.getRGB(0, 0);
		int p[] = origImg.getRGB(0, 0, w, h, null, 0, w);
		int space = 0;
		int countW = 0, maxW = 0;
		ArrayList<Integer> splitPos = new ArrayList<>();
		
		for( int i = 0; i < w; i++ ) {
			if( p[i] == c ) {
				boolean clean = true;
				for( int y = 1; y < h; y++ ) {
					if( p[ y*w +i ] != c) {
						// no empty vertical line of pixels
						clean = false;
						break;
					}
				}
				if( clean ) {
					space += 1;
				} else {
					space = 0;
				}
				if( space == 10 ) {
					space  = 0;
					maxW = Math.max( maxW , countW );
					countW = 0;
					splitPos.add( i-4 );
					for( int y = 0; y < h; y++ ) {
						p[ y*w +i-5 ] = 0xFFFF0000; // ARGB = 0xAARRGGBB = Color.RED
					}
				}
				
			}
			countW += 1;
		}
		System.err.println(maxW+"x"+h);

// Show Splits:
		BufferedImage scanedImg = copyImage( origImg );
		scanedImg.setRGB(0, 0, w, h, p, 0, w);
		JLabel scanedImgLabel = new JLabel( "Scanned: " );
		scanedImgLabel.setIcon( new ImageIcon( scanedImg ) );
		scanedImgLabel.setHorizontalTextPosition( JLabel.LEFT );
		scrollpaneView.add( scanedImgLabel );
		
// Safty exit when no splits found:
		if( splitPos.size() == 0) {
			return;
		}
		
// Splitting Image:		
		int prev = 0;
		BufferedImage[] chunks = new BufferedImage[ splitPos.size() +1 ];
		for( int i=0 ; i<splitPos.size(); i++ ) {
			int s = splitPos.get( i );
			chunks[i] = new BufferedImage( s - prev, h, origImg.getType() );
			Graphics2D g2d = chunks[i].createGraphics();
			g2d.drawImage(	origImg, 
							0, 0, s - prev, h,
							prev, 0, s , h , null);
			prev = s ;
		}
		chunks[ splitPos.size() ] = new BufferedImage( w - prev, h, origImg.getType() );
		Graphics2D g2d = chunks[ splitPos.size() ].createGraphics();
		g2d.drawImage(	origImg, 
						0, 0, w - prev, h,
						prev, 0, w , h , null);
		
// Showing Splits:
		JPanel splitView = new JPanel( new FlowLayout( 10, 2, 2 ) );
		splitView.setBorder( BorderFactory.createTitledBorder( "Split View: (" +chunks.length + ")" ) );
		for( BufferedImage i : chunks ) {
			JLabel j = new JLabel( new ImageIcon( i ) );
			j.setBorder( BorderFactory.createLineBorder( Color.RED ) );
			splitView.add( j );
		}
		scrollpaneView.add( splitView );
		
	}
	

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new JImageSplitter().setVisible(true);
			}
		});

	}

}
