package de.drsilence.tbofiit;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.synth.SynthLookAndFeel;

import de.drsilence.utils.data.ImageManager;
import de.drsilence.utils.data.Json;
import de.drsilence.utils.data.Json.JsonException;
import de.drsilence.utils.data.Json.JsonObject;
import de.drsilence.utils.swing.FlowLayout;
import de.drsilence.utils.swing.JPaningScrollPane;

public class JMainFrame extends JFrame {
	
	private static final long serialVersionUID = 6101846669631909514L;
	
//Strings Output:	
	private static final String APP_TITLE            = "The Binding of Isaac - ItemTracker";
	private static final String APP_SESSINGS_FILE    = "data/settings.json";
	private static final String APP_ITEMDB_FILE      = "data/items.json";
	private static final String APP_SESSINGS_ERROR   = "Can't load 'data/settings.json'!";
	private static final String APP_ITEMDB_ERROR     = "Can't load 'data/items.json'!";
	
//Save rel.:	
	private static final Color  APP_COLOR_TEXT       = Color.BLACK;
	private static final Color  APP_COLOR_BACKGROUND = Color.MAGENTA;
	private static int          APP_WINDOW_XPOS      = 100;
	private static int          APP_WINDOW_YPOS      = 100;
	private static int          APP_WINDOW_WIDTH     = 300;
	private static int          APP_WINDOW_HEIGHT    = 600;
	
//Menu Command Strings:	
	private static final String APP_MENUCMD_CHANGEFG = "CHANGE_FGCOLOR";
	private static final String APP_MENUCMD_CHANGEBG = "CHANGE_BGCOLOR";
	private static final String APP_MENUCMD_CHANGELA = "CHANGE_LACOLOR";
	private static final String APP_MENUCMD_INCCOL   = "INCREASE_COLS";
	private static final String APP_MENUCMD_DECCOL   = "DECREASE_COLS";
	private static final String APP_MENUCMD_EXIT     = "EXIT";
	
	private static final String APP_MENUSTR_CHANGEFG = "Change FG-Color";
	private static final String APP_MENUSTR_CHANGEBG = "Change BG-Color";
	private static final String APP_MENUSTR_CHANGELA = "Change LA-Color";
	private static final String APP_MENUSTR_INCCOL   = "+1 Item-Col";
	private static final String APP_MENUSTR_DECCOL   = "-1 Item-Col";
	private static final String APP_MENUSTR_EXIT     = "Exit";
	private static final String APP_MENUSTR_ERROR    = "Unknown menu command: ";
	
//View Stuff:
	JPanel viewCollectedItems;
	JLabel viewLastAddedCollectedItem;
	
//Objects we need:
	JsonObject   settings;
	JsonObject   itemDB;
	ImageManager imagemanager;
	
	private static JsonObject loadJson(String file) {
		try {
			Object o = Json.parse( ImageManager.getJarInputStream( file ) );
			if( Json.getReturnType(o) == Json.JsonReturnType.Json_Returned_JsonObject ) {
				return (JsonObject)o;
			}
		} catch( JsonException ignore) {
			return null;
		} catch( FileNotFoundException ignore) {
			return null;
		}
		return null;
	}
	
	private void addCollectedItem(int itemID) {
		JLabel label = new JLabel( 
				new ImageIcon( 
						this.imagemanager.getBufferedImage(
								String.format("data/images/collectibles_%03d.png", itemID) ) ) );
		Object o = itemDB.getValue( String.format("%03d", itemID), "" );
		if( Json.getReturnType(o) == Json.JsonReturnType.Json_Returned_JsonObject ) {
			JsonObject jo = (JsonObject)o;
			String itemName = (String)jo.getValue( "name", "Unknown" );
			String itemText = (String)jo.getValue( "text", "Unknown" );
			label.setToolTipText( 
					String.format( "%03d : %s %s %s", 
							itemID, 
							itemName,
							itemText.isEmpty() ? "" : "=", 
							itemText 
							) );
		}
		label.setName("collectedItem");
		label.setBorder( BorderFactory.createLineBorder( Color.BLACK ) );
		if( this.viewLastAddedCollectedItem != null ) {
			this.viewLastAddedCollectedItem.setBorder( null );
		}
		this.viewLastAddedCollectedItem = label;
		this.viewCollectedItems.add( label );
	}
	
	
//*********************************************************************************************************************

	public JMainFrame()  {
		super(APP_TITLE);
		this.setDefaultCloseOperation( EXIT_ON_CLOSE );
		//JMainFrame.initLookAndFeel();
		
//load settings:
		this.settings = loadJson( APP_SESSINGS_FILE );
		if( this.settings == null ) {
			System.err.println( APP_SESSINGS_ERROR );
			System.exit(0);
		}
		setSettings();
		
//load itemDB:
		this.itemDB = loadJson( APP_ITEMDB_FILE );
		if( this.itemDB == null ) {
			System.err.println( APP_ITEMDB_ERROR );
		}
		
//load image manager:
		this.imagemanager = new ImageManager();
		this.imagemanager.setDefaultImg( 
				this.imagemanager.getBufferedImage( 
						(String)this.settings.getValue("defaultIcon", null) ) );
		
//finally do the layout:
		this.doMyLayout();
		this.setPupUpMenu();
		
//last but not least, add shutdown hook for cleanups:		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				cleanup();
			}
		});
	}
	
//*********************************************************************************************************************
	
	private void cleanup() {
		getSettings(); //write settings back to file
	}
	
	private void setSettings() {
		this.setLocation( (Integer)this.settings.getValue( "windowPosX",      APP_WINDOW_XPOS   ) , 
				          (Integer)this.settings.getValue( "windowPosY",      APP_WINDOW_YPOS   ) );
		this.setSize(	  (Integer)this.settings.getValue( "windowWidth",     APP_WINDOW_WIDTH  ) , 
				          (Integer)this.settings.getValue( "windowHeight",    APP_WINDOW_HEIGHT ) );
		this.getContentPane().setForeground( 
			Color.decode(  (String)this.settings.getValue( "textColor",       APP_COLOR_TEXT ) ) );
		this.getContentPane().setBackground( 
			Color.decode(  (String)this.settings.getValue( "backgroundColor", APP_COLOR_BACKGROUND ) ) );
	}
	
	private void getSettings() {
		//TODO:
	}
	
	private static void initLookAndFeel() {
		SynthLookAndFeel lookAndFeel = new SynthLookAndFeel();
		// SynthLookAndFeel load() method throws a checked exception
		// (java.text.ParseException) so it must be handled
		try {
			lookAndFeel.load(JMainFrame.class.getResourceAsStream("synth.xml"), JMainFrame.class);
			UIManager.setLookAndFeel(lookAndFeel);
		}catch (ParseException | UnsupportedLookAndFeelException e) {
			System.err.println( "Couldn't get specified look and feel (" + lookAndFeel + "), for some reason." );
			System.err.println( "Using the default look and feel." );
			e.printStackTrace();
		}
	}
	
	private void doMyLayout() {
		//TODO:
		this.viewCollectedItems = new JPanel( new FlowLayout() );
		this.getContentPane().add( new JPaningScrollPane( this.viewCollectedItems ) );
		
		addCollectedItem(100);
		addCollectedItem(71);
		addCollectedItem(341);
	}
	
	public void setPupUpMenu() {
		final JPopupMenu popup = new JPopupMenu();
		JMenuItem menuitem;
		
		MouseAdapter ma = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
		        maybeShowPopup(e);
		    }

			@Override
		    public void mouseReleased(MouseEvent e) {
		        maybeShowPopup(e);
		    }

		    private void maybeShowPopup(MouseEvent e) {
		        if (e.isPopupTrigger()) {
		            popup.show(e.getComponent(),
		                       e.getX(), e.getY());
		        }
		    }		
		};
		
		ActionListener al = new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				switch( cmd ) {
				//TODO:
				case "EXIT":
					System.exit(0);
					break;
				default:
					System.err.println( APP_MENUSTR_ERROR + cmd);
				}
			}
		};
		
		menuitem = new JMenuItem(APP_MENUSTR_CHANGEFG);
		menuitem.addActionListener(al);
		menuitem.setActionCommand(APP_MENUCMD_CHANGEFG);
		popup.add(menuitem);
		
		menuitem = new JMenuItem(APP_MENUSTR_CHANGEBG);
		menuitem.addActionListener(al);
		menuitem.setActionCommand(APP_MENUCMD_CHANGEBG);
		popup.add(menuitem);
		
		menuitem = new JMenuItem(APP_MENUSTR_CHANGELA);
		menuitem.addActionListener(al);
		menuitem.setActionCommand(APP_MENUCMD_CHANGELA);
		popup.add(menuitem);
		
		popup.addSeparator();
		
		menuitem = new JMenuItem(APP_MENUSTR_INCCOL);
		menuitem.addActionListener(al);
		menuitem.setActionCommand(APP_MENUCMD_INCCOL);
		popup.add(menuitem);
		
		menuitem = new JMenuItem(APP_MENUSTR_DECCOL);
		menuitem.addActionListener(al);
		menuitem.setActionCommand(APP_MENUCMD_DECCOL);
		popup.add(menuitem);
		
		popup.addSeparator();
		
		menuitem = new JMenuItem(APP_MENUSTR_EXIT);
		menuitem.addActionListener(al);
		menuitem.setActionCommand(APP_MENUCMD_EXIT);
		popup.add(menuitem);
		
		// Set this popup to :
		this.addMouseListener(ma);
		this.viewCollectedItems.addMouseListener(ma);
	}
	
	
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new JMainFrame().setVisible(true);
			}
		});
	}
		

}
