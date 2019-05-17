package PacMan;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import pacsim.BFSPath;
import pacsim.PacAction;
import pacsim.PacCell;
import pacsim.PacFace;
import pacsim.PacSim;
import pacsim.PacUtils;
import pacsim.PacmanCell;

/** Pacman Replan
 *  John Lynch 
 */

public class PacSimReplan implements PacAction {
	
	private List<Point> path;
	private int simTime;

	public PacSimReplan (String fname) {
		PacSim sim = new PacSim(fname);
		sim.init(this);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("\nTSP using simple replanning agent by Glinos.");
		System.out.println("\nMaze : " + args[0] + "\n");
		new PacSimReplan( args[0] );
	}
	
	@Override
	public void init() {
		simTime = 0;
		path = new ArrayList<Point>();
	}
	
	@Override
	public PacFace action(Object state) {
		PacCell[][] grid = (PacCell[][]) state;
		PacmanCell pc = PacUtils.findPacman ( grid );
		
		//make sure pacman is in the game.
		if ( pc == null ) return null;
		
		if ( path.isEmpty() ) {
			Point tgt = PacUtils.nearestFood ( pc.getLoc(), grid);
			path = BFSPath.getPath(grid, pc.getLoc(), tgt);
			
			System.out.println("Pac-Man is currently at : [ " + pc.getLoc().x + 
					" , " + pc.getLoc().y + " ] ");
			System.out.println("Setting new target : " + tgt.x + " , " + tgt.y + " ]");
		}
		
		Point next = path.remove(0);
		PacFace face = PacUtils.direction( pc.getLoc(), next);
		System.out.printf("%5d : From [%2d, %2d] go %s%n,", 
				++simTime, pc.getLoc().x, pc.getLoc().y, face);
		return face;
		
	}

}
