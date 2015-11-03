package de.drsilence.tbofiit;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import de.drsilence.utils.data.ImageManager;
import de.drsilence.utils.data.Json;
import de.drsilence.utils.data.Json.JsonException;
import de.drsilence.utils.data.Json.JsonObject;

public class JMainFrame extends JFrame {
	
	private static final long serialVersionUID = 6101846669631909514L;
	
	private static final String APP_TITLE          = "The Binding of Isaac - ItemTracker";
	private static final String APP_SESSINGS_FILE  = "data/settings.json";
	private static final String APP_SESSINGS_ERROR = "Can't load 'data/settings.json'!";
	
	private static final Color  APP_COLOR_TEXT       = Color.BLACK;
	private static final Color  APP_COLOR_BACKGROUND = Color.MAGENTA;
	private static int          APP_WINDOW_XPOS      = 100;
	private static int          APP_WINDOW_YPOS      = 100;
	private static int          APP_WINDOW_WIDTH     = 300;
	private static int          APP_WINDOW_HEIGHT    = 600;
	
	JsonObject   settings;
	ImageManager imagemanager;
	
	private JsonObject loadJson(String file) {
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

	public JMainFrame()  {
		super(APP_TITLE);
		this.setDefaultCloseOperation( EXIT_ON_CLOSE );
		this.setPupUpMenu();
		
		this.settings = loadJson( APP_SESSINGS_FILE );
		if( this.settings == null ) {
			System.err.println( APP_SESSINGS_ERROR );
			System.exit(0);
		}
		setSettings();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				cleanup();
			}
		});
	}
	
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
					System.err.println( "Unknown menu command: " + cmd);
				}
			}
		};
		
		menuitem = new JMenuItem("Change FG-Color");
		menuitem.addActionListener(al);
		menuitem.setActionCommand("CHANGE_FGCOLOR");
		popup.add(menuitem);
		
		menuitem = new JMenuItem("Change BG-Color");
		menuitem.addActionListener(al);
		menuitem.setActionCommand("CHANGE_BGCOLOR");
		popup.add(menuitem);
		
		menuitem = new JMenuItem("Change LA-Color");
		menuitem.addActionListener(al);
		menuitem.setActionCommand("CHANGE_LACOLOR");
		popup.add(menuitem);
		
		popup.addSeparator();
		
		menuitem = new JMenuItem("+1 Item-Col");
		menuitem.addActionListener(al);
		menuitem.setActionCommand("INCREASE_COLS");
		popup.add(menuitem);
		
		menuitem = new JMenuItem("-1 Item-Col");
		menuitem.addActionListener(al);
		menuitem.setActionCommand("DECREASE_COLS");
		popup.add(menuitem);
		
		popup.addSeparator();
		
		menuitem = new JMenuItem("Exit");
		menuitem.addActionListener(al);
		menuitem.setActionCommand("EXIT");
		popup.add(menuitem);
		
		// Set this popup to :
		this.addMouseListener(ma);
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
