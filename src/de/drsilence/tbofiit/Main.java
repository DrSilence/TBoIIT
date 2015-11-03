package de.drsilence.tbofiit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import de.drsilence.utils.data.ImageManager;
import de.drsilence.utils.data.Json;
import de.drsilence.utils.data.Json.JsonException;
import de.drsilence.utils.data.Json.JsonObject;
import de.drsilence.utils.data.Json.JsonReturnType;
import de.drsilence.utils.io.FileWatcher;
import de.drsilence.utils.io.FileWatcher.FileWatcherActionListener;
import de.drsilence.utils.swing.FlowLayout;
import de.drsilence.utils.swing.JPaningScrollPane;
import de.drsilence.utils.swing.SpringUtilities;

public class Main extends JFrame {
	
	private static final long serialVersionUID = 8272292027235994L;
	
	// statics:
	public static ImageManager imageManager = new ImageManager();
	public static final String LOG_FILE = Main.getLogFileName();
//	public static final String LOG_FILE = "/home/drsilence/.local/share/binding of isaac afterbirth/log.txt";
//	public static final String LOG_FILE = "/home/drsilence/.local/share/binding of isaac rebirth/log.txt";
//	public static final String LOG_FILE = "/home/drsilence/.local/share/binding of isaac rebirth/run-guppy.log.txt";
	public static long LOG_SIZE = 0;
//	public static ImageIcon IMG_NOT_FOUND;
	public static ImageIcon IMG_GUPPY;
	public static ImageIcon IMG_LOTF;
	
	// data related:
	private JsonObject itemDB;
	private BufferedReader logReader;
	private Thread th;
	
	// data related -> setting file:
	private boolean doLongFloorNames = false;
	private int     lotfNeeded       = 3;
	private int     guppyNeeded      = 3;
	private int     itemPanelCols    = 3;
	private int     scrollDelay      = 10000;
	private int     scrollSpeed      = 100;
	private Color   lastAddedColor   = Color.WHITE;
	private Color   backgroundColor  = Color.MAGENTA;
	
	// game related:
	private ArrayList<String> visitedRooms;
	private ArrayList<String> collectedItems;
	private int roomsInFloor;
	private int guppyCounter;
	private int lotfCounter; // Lord of the Flies
	
	// view stuff:
	public JLabel seedLabel;
	public JLabel floorLabel;
	public JLabel roomcounLabel;
	public JLabel curseLabel;
	public JPanel itemsPanel;
	public JLabel lastAdded;
	
	public JLabel    lastAddedNameLabel;
	public JTextArea lastAddedDesc1Label;
	public JLabel    lastAddedDesc2Label;
	
	public JLabel guppyLabel;
	public JLabel lotfLabel;
	
	private JPopupMenu popup;
	
	public static ImageIcon getIcon(String itemId) {
//		try {
//			ImageIcon i = new ImageIcon( ImageIO.read( new File("data/images/collectibles_"+itemId+".png") ) );
//			return i;
//		} catch (IOException ignore) {
//		}
//		return IMG_NOT_FOUND;
		return new ImageIcon( Main.imageManager.getBufferedImage( "data/images/collectibles_"+itemId+".png") );
	}
	
	public static String getLogFileName() {
		
		String os = System.getProperty("os.name");
		System.err.println(os);
		
		if( ! "Linux".equalsIgnoreCase( os ) ) {
			// Windoof :(
			JFileChooser fc = new JFileChooser();
			fc.setFileFilter( new FileFilter() {
				@Override
				public String getDescription() {
					return "*.txt";
				}
				@Override
				public boolean accept(File f) {
					String ext = null;
			        String s = f.getName();
			        int i = s.lastIndexOf('.');

			        if (i > 0 &&  i < s.length() - 1) {
			            ext = s.substring(i+1).toLowerCase();
			        }
			        if( "txt".equalsIgnoreCase( ext ) ) {
			        	return true;
			        }
					return f.isDirectory() ? true : false;
				}
			});
			fc.setDialogTitle("Select The Binding of Isaac Log File");
			fc.showOpenDialog( null );
			String ret = fc.getSelectedFile().getAbsolutePath();
			if( ret == null ) {
				System.exit( 0 );
			}
			return ret;
		}
		
		// Linux <3
		File rebirth = new File( System.getProperty("user.home") + File.separator + ".local" + File.separator + "share" + File.separator +
				"binding of isaac rebirth" + File.separator + "log.txt" );
		File afterbirth = new File( System.getProperty("user.home") + File.separator + ".local" + File.separator + "share" + File.separator +
				"binding of isaac afterbirth" + File.separator + "log.txt" );
		return 	afterbirth.exists() && afterbirth.isFile() ? afterbirth.getAbsolutePath() : rebirth.getAbsolutePath();
	}
	
	private static String floorIdToNameShort(String fid) {
		switch( fid ) {
			case "f1": return "B1";
			case "f2": return "B2";
			case "f3": return "C1";
			case "f4": return "C2";
			case "f5": return "D1";
			case "f6": return "D2";
			case "f7": return "W1";
			case "f8": return "W2";
			case "f9": return "SHEOL";
			case "f10": return "CATH";
			case "f11": return "DARK";
			case "f12": return "CHEST";
			case "f1x": return "BXL";
			case "f3x": return "CXL";
			case "f5x": return "DXL";
			case "f7x": return "WXL";
			default: return "Unknown "+fid;
		}
	}
	
	private static String floorIdToNameLong(String fid) {
		//TODO:
		switch( fid ) {
			case "f1": return "Basement 1";
			case "f2": return "Basement 2";
			case "f3": return "Catacombs 1";
			case "f4": return "Catacombs 2";
			case "f5": return "Deep 1";
			case "f6": return "Deep 2";
			case "f7": return "Womb 1";
			case "f8": return "Womb 2";
			case "f9": return "Sheol";
			case "f10": return "Cathedral";
			case "f11": return "Dark Room";
			case "f12": return "The Chest";
			case "f1x": return "BXL";
			case "f3x": return "CXL";
			case "f5x": return "DXL";
			case "f7x": return "WXL";
			default: return "Unknown "+fid;
		}
	}
	
	private String floorIdToName(String fid) {
		if( this.doLongFloorNames ) {
			return floorIdToNameLong(fid);
		}
		return floorIdToNameShort(fid);
	}
	
	public void read() throws IOException {
		while( true ) {
			String s = logReader.readLine();
			if( s == null ) {
				break;
			}
			
			if( s.startsWith("[warn] ") ) {
				continue;
			}
			
			if( s.indexOf("PersistentGameData") > 0 ) {
				continue;
			}
			
			// "SteamCloud could not find or open ab_gamestate1.dat. No Game State detected."
			if( s.startsWith("SteamCloud could ") ) {
				continue;
			}
			
			// "Could not find expanded path for font/MPlus_12b.fnt."
			if( s.startsWith("Could not find ") ) {
				continue;
			}
			
			// "Seed 22 removed from SaveState"
			if( s.startsWith("Seed ") && s.indexOf("SaveState")>0 ) {
				continue;
			}
						
			// "AnmCache memory usage: 13075216 bytes in 1009 entries."
			if( s.startsWith("AnmCache memory usage:") ) {
				continue;
			}
			
			// "music stopped playing"
			if( s.startsWith("music stopped playing") ) {
				continue;
			}
			
			// "Queued Path music/the depths.ogg"
			if( s.startsWith("Queued Path ") ) {
				continue;
			}
			
			// "delete 783 rooms."
			if( s.startsWith("delete ") && s.indexOf("rooms")>0 ) {
				continue;
			}
			
			// "generate..."
			if( s.startsWith("generate...") ) {
				continue;
			}
			
			// "place_room: shape 11"
			if( s.startsWith("place_room: ") ) {
				continue;
			}
			
			// "placing rooms..."
			if( s.startsWith("placing rooms...") ) {
				continue;
			}
			
			// "Map Generated in 1 Loops"
			if( s.startsWith("Map Generated in ") ) {
				continue;
			}
			
			// "Level::Init m_Stage 8, m_AltStage 1 Seed 4242964686"
			if( s.startsWith("Level::Init ") ) {
				String floorId = s.split(" ")[2].replace( ',', ' ' ).trim();
				this.floorLabel.setText( floorIdToName( "f" + floorId ) );
				this.roomcounLabel.setText( "" );
				this.curseLabel.setText( "" );
				continue;
			}
			
			// "RNG Start Seed: 8DMZ D1MA (4281871904)"
			if( s.startsWith("RNG Start Seed:") ) {
				// New Game started. ?
				String seed = s.substring(s.indexOf(':')+1, s.indexOf( '(' )-1).trim();
				this.collectedItems.clear();
				this.itemsPanel.removeAll();
				this.seedLabel.setText( seed );
				this.guppyCounter = 0;
				this.lotfCounter  = 0;
				this.guppyLabel.setText( "0 / " + this.guppyNeeded );
				this.lotfLabel.setText( "0 / " + this.lotfNeeded );
				this.guppyLabel.setIcon( null );
				this.lotfLabel.setIcon( null );
				continue;
			}
			
			// "20 rooms in 12 loops"
			if( s.indexOf(" rooms in ") > 0 ) {
				this.roomsInFloor = Integer.parseInt( s.split(" ")[0] );
				this.visitedRooms.clear();
				this.roomcounLabel.setText( this.visitedRooms.size() + " / " + roomsInFloor );
				continue;
			}
			
			// "[RoomConfig] load stage <nr>: <name>
			// "[RoomConfig] load stage 11: Dark Room"
			if( s.startsWith("[RoomConfig]") ) {
				String floorName = s.substring( s.indexOf( ':' ) +1 ).trim();
				this.floorLabel.setText( floorName );
				continue;
			}
			
			// "Room <nr>([New Room|Start Room| .* copy|<name>])
			// "Room 1.192(New Room)"
			// "Room 1.2(Start Room)"
			// "Room 1.59()"
			// "Room 4.7( (copy))"
			// "Room 4.7( <txt> copy)"
			if( s.startsWith("Room ") ) {
				continue;
			}
			
			// "Spawn Entity with Type(<nr>), Variant(<nr>), Pos(320.00,280.00)
			// "Spawn Entity with Type(59), Variant(0), Pos(320.00,280.00)"
			// "Spawn Entity with Type(59), Variant(0), Pos(440.00,280.00)"
			// "Spawn Entity with Type(78), Variant(1), Pos(320.00,280.00)"
			if( s.startsWith("Spawn Entity with Type") ) {
				continue;
			}
			
			// "deathspawn_boss, color idx -1"
			if( s.startsWith("deathspawn_boss, ") ) {
				continue;
			}
			
			// "Mom clear time: 48767"
			// "Par time: 36000"
			if( s.startsWith("Mom clear time: ") || s.startsWith("Par time: ") ) {
				continue;
			}
			
			// "Game Over. Killed by (9.0) spawned by (78.1) damage flags (0)"
			if( s.startsWith("Game Over. Killed by ") ) {
				continue;
			}
			
			// "SpawnRNG seed: <number>"
			// "SpawnRNG seed: 3691693045"
			if( s.startsWith("SpawnRNG seed: ") ) {
				String rngSeed = s.substring( s.indexOf(':') +1 ).trim();
				if( !this.visitedRooms.contains(rngSeed) ) {
					this.visitedRooms.add( rngSeed );
					this.roomcounLabel.setText( this.visitedRooms.size() + " / " + roomsInFloor );
				}
				continue;
			}
			
			// "Curse of the <name>"
			// "Curse of the Unknown"
			// "Curse of the Lost!"
			// "Curse of <name>"
			if( s.startsWith("Curse of ") ) {
				String floorCurse = s.trim();
				this.curseLabel.setText( floorCurse );
				continue;
			}
			
			
			// "Adding collectible 142 (Scapular)"
			if( s.startsWith("Adding collectible ") ) {
				// New Item collected.
				int itemId = Integer.parseInt(s.substring(19, s.indexOf( '(' )).trim());
				String itemIds = String.format( "%03d", itemId );
				if( !this.collectedItems.contains(itemIds) ) {
					this.collectedItems.add( itemIds );
					ImageIcon icon = getIcon( itemIds );
					Object o = itemDB.getValue( itemIds, "" );
					if( Json.getReturnType(o) == Json.JsonReturnType.Json_Returned_JsonObject ) {
						//extend ItemString:
						JsonObject jo = (JsonObject)o;
						String itemName = (String)jo.getValue( "name", "Unknown" );
						String itemText = (String)jo.getValue( "text", "Unknown" );
						itemIds += "= "+itemName+": "+itemText;
						this.lastAddedNameLabel.setText( itemName + ":" );
						this.lastAddedDesc1Label.setText( itemText );
						//this.lastAddedDesc2Label.setText( itemText );
						
						//count guppy pieces: 
						if( (Boolean)jo.getValue( "guppy", Boolean.FALSE ) ) {
							this.guppyCounter += 1;
							this.guppyLabel.setText( this.guppyCounter + " / " + this.guppyNeeded );
							if( this.guppyCounter == this.guppyNeeded ) {
								this.guppyLabel.setIcon( Main.IMG_GUPPY );
								this.guppyLabel.setHorizontalTextPosition( JLabel.LEFT );
							}
						}
						//count lord of the flies pieces:
						if( (Boolean)jo.getValue( "lotf", Boolean.FALSE ) ) {
							this.lotfCounter += 1;
							this.lotfLabel.setText( this.lotfCounter + " / " + this.lotfNeeded );
							if( this.lotfCounter == this.lotfNeeded ) {
								this.lotfLabel.setIcon( Main.IMG_LOTF );
								this.lotfLabel.setHorizontalTextPosition( JLabel.LEFT );
							}
						}
					}
					JLabel l = new JLabel( icon, SwingConstants.LEADING );
					l.setBorder( BorderFactory.createLineBorder( this.lastAddedColor, 1 ) );
					l.setToolTipText( itemIds );
					if( this.lastAdded != null) {
						this.lastAdded.setBorder( null );
					}
					this.itemsPanel.add( l );
					this.lastAdded = l;
				}
				continue;
			}
			
			System.out.println(s);
			
		} //while
		itemsPanel.validate();
		this.repaint();
	}
	
	public Main() throws JsonException, IOException {
		this.setTitle("The Binding of Isaac - ItemTracker");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize( new Dimension(300,600) );
//		Main.IMG_NOT_FOUND = new ImageIcon( Main.imageManager.getBufferedImage( "data/images/questionmark.png" ) );
		Main.IMG_GUPPY     = new ImageIcon( Main.imageManager.getBufferedImage( "data/images/Guppy_App.png") );
		Main.IMG_LOTF      = new ImageIcon( Main.imageManager.getBufferedImage( "data/images/Lord_Of_Flies_App.png") );
		Main.imageManager.setDefaultImg( Main.imageManager.getBufferedImage( "data/images/questionmark.png" ) );
		
		this.itemsPanel    = new JPanel( new FlowLayout( this.itemPanelCols ) );
		this.itemsPanel.setBackground( this.backgroundColor );
		JPaningScrollPane jsp = new JPaningScrollPane(	this.itemsPanel,
														ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
														ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jsp.setBorder( null );
		jsp.setAntialias( false );
		jsp.setTimes( this.scrollDelay, this.scrollSpeed );
		
		JPanel p = new JPanel( new SpringLayout() );
		this.seedLabel     = new JLabel();
		this.floorLabel    = new JLabel();
		this.roomcounLabel = new JLabel();
		this.curseLabel    = new JLabel();
		this.guppyLabel    = new JLabel();
		this.lotfLabel     = new JLabel();
		p.add( new JLabel( "Seed:", JLabel.TRAILING ) );
		p.add( this.seedLabel );
		p.add( new JLabel( "Floor:", JLabel.TRAILING ) );
		p.add( this.floorLabel );
		p.add( new JLabel( "Room:", JLabel.TRAILING ) );
		p.add( this.roomcounLabel );
		p.add( new JLabel( "Curse:", JLabel.TRAILING ) );
		p.add( this.curseLabel );
		p.add( new JLabel( "Guppy:", JLabel.TRAILING ) );
		p.add( this.guppyLabel );
		p.add( new JLabel( "LotF:", JLabel.TRAILING ) );
		p.add( this.lotfLabel );
		p.setBackground( this.backgroundColor );
		SpringUtilities.makeCompactGrid(p, 6, 2, 5, 5, 5, 5);
		
		JPanel pp = new JPanel( new SpringLayout() );
		this.lastAddedNameLabel  = new JLabel( "", JLabel.TRAILING );
//		this.lastAddedDesc1Label = new JLabel();
		this.lastAddedDesc1Label = new JTextArea();
		this.lastAddedDesc1Label.setWrapStyleWord(true);
		this.lastAddedDesc1Label.setLineWrap(true);
		this.lastAddedDesc1Label.setOpaque(false);
		this.lastAddedDesc1Label.setEditable(false);
		this.lastAddedDesc1Label.setFocusable(false);
		this.lastAddedDesc1Label.setFont( lastAddedNameLabel.getFont() );
		this.lastAddedDesc1Label.setBorder( lastAddedNameLabel.getBorder() );
		this.lastAddedDesc2Label = new JLabel();
		pp.add( this.lastAddedNameLabel );
		pp.add( this.lastAddedDesc1Label );
		pp.add( new JLabel("") ); //blank placeholder
		pp.add( this.lastAddedDesc2Label );
		pp.setBackground( this.backgroundColor );
		SpringUtilities.makeCompactGrid(pp, 2, 2, 5, 5, 5, 5);
		
		Container contentPane = this.getContentPane();
		contentPane.setLayout( new BorderLayout() );
		contentPane.setBackground( this.backgroundColor );
		contentPane.add(   p, BorderLayout.NORTH );
		contentPane.add( jsp, BorderLayout.CENTER );
		contentPane.add(  pp, BorderLayout.SOUTH );
		
		this.collectedItems = new ArrayList<String>();
		this.visitedRooms   = new ArrayList<String>();
		this.lotfCounter    = 0;
		this.guppyCounter   = 0;
		this.roomsInFloor   = 0;
		this.lastAdded      = null;
		
		// Init Item Database:
		Object o = Json.parse( ImageManager.getJarInputStream("data/items.json") );
		if( Json.getReturnType( o ) != JsonReturnType.Json_Returned_JsonObject ) {
			System.err.println("Error while parsing 'items.json'!");
			return;
		}
		System.err.println("'items.json' parsed !");
		itemDB = (JsonObject)o;
		
		//get settings:
		o = Json.parse( ImageManager.getJarInputStream("data/settings.json") );
		if( Json.getReturnType( o ) != JsonReturnType.Json_Returned_JsonObject ) {
			System.err.println("Error while parsing 'settings.json'!");
			return;
		}
		System.err.println("'settings.json' parsed !");
		//settings = (JsonObject)o;
		
		// Init LogReader and overread present lines:
		logReader = new BufferedReader(new FileReader(LOG_FILE));
		read();
		
		this.th = new Thread(new FileWatcher(new File(LOG_FILE), new FileWatcherActionListener() {
			@Override
			public void onModify(final File target) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							long fl = target.length();
							if( fl < LOG_SIZE ) {
								//file is now smaller! -> reset Filereader!
								if(logReader!=null) logReader.close();
								logReader = new BufferedReader(new FileReader(LOG_FILE));
								System.err.println("Filesize changed ! -> Reset FileReader...");
							}

							read();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}));
		th.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				cleanUp();
			}
		});
		setPupUpMenu();
	}
		
	public void cleanUp() {	
		// Cleanup on close
		th.interrupt();
		try {
			th.join();
			logReader.close();
		} catch (InterruptedException|IOException ignore) {
		}
	}
	
	public void setPupUpMenu() {
		this.popup = new JPopupMenu();
		JMenuItem menuitem;
		
		ActionListener al = new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				Color newColor;
				FlowLayout flm;
				switch(cmd) {
					case "EXIT":
						System.exit(0);
						break;
						
					case "CHANGE_BGCOLOR":
						newColor = JColorChooser.showDialog(
												Main.this,
												"Choose Background Color",
												Main.this.getContentPane().getBackground());
						if( newColor == null) {
							break;
						}
						Main.this.getContentPane().setBackground(newColor);
						itemsPanel.setBackground(newColor);
						seedLabel.getParent().setBackground(newColor);
						lastAddedNameLabel.getParent().setBackground(newColor);
						break;
						
					case "CHANGE_FGCOLOR":
						newColor = JColorChooser.showDialog(
												Main.this,
												"Choose Background Color",
												Main.this.seedLabel.getForeground());
						if( newColor == null) {
							break;
						}
						for( Component comp : Main.this.seedLabel.getParent().getComponents() ) {
							comp.setForeground(newColor);
						}
						for( Component comp : Main.this.lastAddedNameLabel.getParent().getComponents() ) {
							comp.setForeground(newColor);
						}
						break;
						
					case "CHANGE_LACOLOR":
						newColor = JColorChooser.showDialog(
												Main.this,
												"Choose Background Color",
												Main.this.seedLabel.getForeground());
						if( newColor == null) {
							break;
						}
						Main.this.lastAddedColor = newColor;
						if( Main.this.lastAdded != null ) {
							Main.this.lastAdded.setBorder( BorderFactory.createLineBorder( newColor ) );
						}
						break;
						
					case "INCREASE_COLS":
						Main.this.itemPanelCols += 1;
						flm = (FlowLayout)Main.this.itemsPanel.getLayout();
						flm.setMaxColCount( Main.this.itemPanelCols );
						Main.this.itemsPanel.doLayout();
						Main.this.getContentPane().revalidate();
						break;
						
					case "DECREASE_COLS":
						Main.this.itemPanelCols -= Main.this.itemPanelCols > 0 ? 1 : 0;
						flm = (FlowLayout)Main.this.itemsPanel.getLayout();
						flm.setMaxColCount( Main.this.itemPanelCols );
						Main.this.itemsPanel.doLayout();
						Main.this.getContentPane().revalidate();
						break;
						
					default:
						System.err.println("[WARNING] JPopupMenu.ActionListener: unknown command -> " + cmd);
				}
				
			}
		};
		
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
		
		menuitem = new JMenuItem("Change FG-Color");
		menuitem.addActionListener(al);
		menuitem.setActionCommand("CHANGE_FGCOLOR");
		this.popup.add(menuitem);
		
		menuitem = new JMenuItem("Change BG-Color");
		menuitem.addActionListener(al);
		menuitem.setActionCommand("CHANGE_BGCOLOR");
		this.popup.add(menuitem);
		
		menuitem = new JMenuItem("Change LA-Color");
		menuitem.addActionListener(al);
		menuitem.setActionCommand("CHANGE_LACOLOR");
		this.popup.add(menuitem);
		
		this.popup.addSeparator();
		
		menuitem = new JMenuItem("+1 Item-Col");
		menuitem.addActionListener(al);
		menuitem.setActionCommand("INCREASE_COLS");
		this.popup.add(menuitem);
		
		menuitem = new JMenuItem("-1 Item-Col");
		menuitem.addActionListener(al);
		menuitem.setActionCommand("DECREASE_COLS");
		this.popup.add(menuitem);
		
		this.popup.addSeparator();
		
		menuitem = new JMenuItem("Exit");
		menuitem.addActionListener(al);
		menuitem.setActionCommand("EXIT");
		this.popup.add(menuitem);
		
		// Set this popup to :
		this.addMouseListener(ma);
		this.itemsPanel.addMouseListener(ma);
		
	}
	
	public static void main(String[] args) {
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					new Main().setVisible(true);
				} catch (JsonException | IOException e) {
					e.printStackTrace();
				}
			}
		});
		
	}

}
