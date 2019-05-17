/* 
 * University of Central Florida
 * CAP4630 - Spring 2019
 * Author: John Lynch
 */

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
// import pacsim.BFSPath;
// import pacsim.PacAction;
// import pacsim.PacCell;
// import pacsim.PacFace;
// import pacsim.PacSim;
// import pacsim.PacUtils;
// import pacsim.PacmanCell;
// import pacsim.PacMode;
// import pacsim.GhostCell;
// import pacsim.BlinkyCell;
// import pacsim.InkyCell;
// import pacsim.PowerCell;
import pacsim.*;

public class PacSimMinimax implements PacAction {

	//optional classes
	private final int WIN_VALUE = 100;
	private final int LOSS_VALUE = -100;
	private static int DEPTH;
	private static int fearCounter;
	private static PacFace nextFace;
	private static int powerTick;

	public PacSimMinimax( int depth, String fname, int te, int gran, int max ){

		//optional variables
		PacSim sim = new PacSim( fname, te, gran, max );
		sim.init(this);
	}

	public static void main(String[] args) { 
		String fname = args[0];
		int depth = Integer.parseInt(args[1]);
		DEPTH = depth;
		int te = 0;
		int gr = 0;
		int ml = 0;

		if ( args.length == 5 ) {
			te = Integer.parseInt( args[2] );
			gr = Integer.parseInt( args[3] );
			ml = Integer.parseInt( args[4] );
		}

		new PacSimMinimax( depth, fname, te, gr, ml );

		System.out.println("\nAdversarial Search using Minimax by John Lynch");
		System.out.println("\n\t\tGame Board: " + fname);
		System.out.println("\n\t\tSearch Depth : " + depth + "\n");
		if ( te > 0 ) {
			System.out.println("\tPreliminary runs: " + te
				+ "\n\tGranularity\t: " + gr
				+ "\n\tMax move limit\t: " + ml
				+ "\n\nPreliminary run results :\n");
		}
	}

	@Override
	public void init() {}

	@Override
	public PacFace action( Object state ) {
		PacCell[][] grid = (PacCell[][]) state;
		nextFace = null;

		PacmanCell pc = PacUtils.findPacman ( grid );

		List<Point> ghosts = PacUtils.findGhosts ( grid );
		BlinkyCell blinky;
		InkyCell inky;
		if ( grid[ghosts.get(0).x][ghosts.get(0).y] instanceof BlinkyCell ){
			blinky = new BlinkyCell(ghosts.get(0).x, ghosts.get(0).y) ;
			inky = new InkyCell(ghosts.get(1).x, ghosts.get(1).y) ;
		} else {
			blinky = new BlinkyCell(ghosts.get(1).x, ghosts.get(1).y) ;
			inky = new InkyCell(ghosts.get(0).x, ghosts.get(0).y) ;
		}
		
		if ( pc == null ) return null;

		int h = calcMax( grid, 0 );

		PacCell n = PacUtils.neighbor(nextFace, pc, grid);

		return nextFace	;
	}

	int calcMax( PacCell[][] grid, int d ){
		// If we have hit our maximum depth, JUST return the current cost
		if ( d == DEPTH ){
			return evaluateBoard( grid );
		}

		// This function looks for the max value, lets set a variable to store it
		int max = Integer.MIN_VALUE;

		// Pacman is going to make a move
		// How? By checking his neighbors and moving to them
		// So, iterate through pacman's neighbors.
		PacmanCell pc = PacUtils.findPacman ( grid );
		List<Point> ghosts = PacUtils.findGhosts ( grid );
		BlinkyCell blinky;
		InkyCell inky;
		if ( grid[ghosts.get(0).x][ghosts.get(0).y] instanceof BlinkyCell ){
			blinky = new BlinkyCell(ghosts.get(0).x, ghosts.get(0).y) ;
			inky = new InkyCell(ghosts.get(1).x, ghosts.get(1).y) ;
		} else {
			blinky = new BlinkyCell(ghosts.get(1).x, ghosts.get(1).y) ;
			inky = new InkyCell(ghosts.get(0).x, ghosts.get(0).y) ;
		}

		for ( PacFace dirPac : PacFace.values() ) {  //Iterate through each cardinal direction available to pacman
			int newMin = 0;
			int closestPellet = 0;
			Point cPellet = PacUtils.nearestGoody( PacUtils.findPacman( grid ).getLoc(), grid );
			if ( cPellet == null )
				return 100;

			closestPellet = BFSPath.getPath(grid, pc.getLoc(), cPellet ).size();
			PacCell neighbor = PacUtils.neighbor( dirPac, pc, grid );
			// Pacman can travel through walls or house, he should also not run INTO a ghost should it be next to him
			if ( neighbor instanceof pacsim.WallCell || neighbor instanceof pacsim.HouseCell ) continue; //skip this neighbor

			if ( neighbor instanceof pacsim.GhostCell ) return -1000;

			// make sure we aren't under 4 tiles away from a ghost
			int distBlinky = BFSPath.getPath( grid, neighbor.getLoc(), blinky.getLoc() ).size();
			int distInky = BFSPath.getPath( grid, neighbor.getLoc(), inky.getLoc() ).size();

			if ( neighbor instanceof pacsim.PowerCell ) { // if we're going to hit a powercell, set our ghosts to fearful
				powerTick += blinky.getFearTimer();
				blinky.setFearful();
				inky.setFearful();
			}

			// If the neighbor is good to test, then let's clone the grid and move pacman
			PacCell[][] newGrid = PacUtils.movePacman( pc.getLoc(), neighbor.getLoc(), grid );

			int newClosePellet = 0;
			Point newPellet = PacUtils.nearestGoody( PacUtils.findPacman( newGrid ).getLoc(), newGrid );

			/* Pacman needs to be level-headed when danger is close by,
			 * If he detects that one or more ghosts are closer than 4 tiles
			 * away, he'll deal with them by prioritizing moving away from the
			 * closest ghost by a negative factor of its distance from pacman.
			*/
			if ( distBlinky <= 4 || distInky <= 4 ){
				if ( distBlinky < distInky )
					newMin += -150 * (4 - distBlinky );
				if ( distInky < distBlinky )
					newMin += -150 * (4 - distInky );
				if ( distInky == distBlinky )
					newMin += -175;
					
			}

			if ( newPellet == null )
				newMin += 10;
			else 
				newClosePellet = BFSPath.getPath(newGrid, PacUtils.findPacman( newGrid ).getLoc(), newPellet ).size();

			// with this new iteration of the grid, now we should plan the next ghosts move,
			newMin += calcMin( newGrid, d );

			if ( newClosePellet < closestPellet ) newMin += 5;

			// we want to encourage pacman to try cells that have pellets/goodies over empty cells
			if ( neighbor instanceof pacsim.FoodCell || neighbor instanceof PowerCell ) newMin += 15;

			// we can also do this by giving more value to moves that decrease the distance to his nearest pellet.


			// with the minimum value of this tree acquired, check if it's greater than our last one, if so, then replace it
			if ( newMin > max ){
				max = newMin;
				if ( d == 0)
					nextFace = dirPac;
			}

		}
		return max;
	}

	int calcMin( PacCell[][] grid, int d ){
		// Now that we've moved pacman, we need to move the ghosts

		//but first, we need to find and declare them.
		List<Point> ghosts = PacUtils.findGhosts ( grid );
		BlinkyCell blinky;
		InkyCell inky;
		if ( grid[ghosts.get(0).x][ghosts.get(0).y] instanceof BlinkyCell ){
			blinky = new BlinkyCell(ghosts.get(0).x, ghosts.get(0).y) ;
			inky = new InkyCell(ghosts.get(1).x, ghosts.get(1).y) ;
		} else {
			blinky = new BlinkyCell(ghosts.get(1).x, ghosts.get(1).y) ;
			inky = new InkyCell(ghosts.get(0).x, ghosts.get(0).y) ;
		}

		int min = Integer.MAX_VALUE;

		// we'll want to find the distance pacman is from the ghosts once they've moved, if they're ever 
		// closer than 3 tiles, pacman will want to move away in order to preserve his life.
		PacmanCell pc;
		int distPacman = 0; 
		// same as before with pacman, we need to test each neighbor cells
		// However since blinky and inky move independent of eachother, we have to nest their iterations
		// In the below case, inky moves as a function of blinky. 
		for ( PacFace dirBlinky : PacFace.values() ) {
			// set blinky's neighbor
			PacCell nBlinky = PacUtils.neighbor( dirBlinky, blinky, grid );

			//iterate to inky ONLY if the neighbor is valid. No WallsCells
			if ( nBlinky instanceof pacsim.WallCell ) continue; 
			if ( nBlinky instanceof pacsim.PacmanCell ) return -1000;
			

			// if possible neighbor create a test grid
			PacCell[][] newGrid = new PacCell[grid.length][grid[0].length];
			newGrid = PacUtils.moveGhost( blinky.getLoc(), nBlinky.getLoc(), grid ); // move blinky

			// Now that we have a new grid, find how close Blinky is to pacman
			pc = PacUtils.findPacman( newGrid );
			distPacman = BFSPath.getPath( newGrid, pc.getLoc(), nBlinky.getLoc() ).size();

			if ( distPacman < 5 ){
				return -150;	
			}
			
			for ( PacFace dirInky : PacFace.values() ) {
				int newMax;
				//set inky's neighbor
				PacCell nInky = PacUtils.neighbor( dirInky, inky, grid );
				//skip if neighbor cell is a wall
				if ( nInky instanceof pacsim.WallCell ) continue;

				if ( nInky instanceof pacsim.PacmanCell ) return -1000;

				// otherwise, move inky
				newGrid = PacUtils.moveGhost( inky.getLoc(), nInky.getLoc(), newGrid );

				pc = PacUtils.findPacman( newGrid );
				distPacman = BFSPath.getPath(grid, pc.getLoc(), nInky.getLoc() ).size();

				if ( distPacman < 4 ) {
					// HOWEVER, if they're mode is set to fear we can use this opportunity to maybe
					// reset the ghost
					return -150;
				}

				// now that we've moved the ghosts, we need to find how pacman will respond,
				// OR if we've finally reached our depth limit, determine which path to use.
				newMax = calcMax( newGrid, d + 1 );

				if ( newMax < min )
					min = newMax;
			}
		}

		return min;


	}

	/* Evaluation Procedure:
	 	Calculate the distance between pacman and the ghosts and nearest pellet on the board.
	 	Subtract them from set win & loss values while adding a negative value for the number of pellets
	 	left on the board. Essentially the reward for getting more pellets returns a higher evaluation value.
	 	Pacman also will disregard the distance of the ghosts if they are currently in FEAR mode, as they cannot kill him.
	*/
	public int evaluateBoard( PacCell[][] grid ) {
		PacmanCell pc = PacUtils.findPacman ( grid );

		List<Point> ghosts = PacUtils.findGhosts ( grid );
		GhostCell blinky = new BlinkyCell(ghosts.get(0).x, ghosts.get(0).y);
		GhostCell inky = new InkyCell(ghosts.get(1).x, ghosts.get(1).y);

		Point closestPellet = PacUtils.nearestGoody(pc.getLoc(), grid);
		int distPellet = 0;
		if (closestPellet != null ){ // We're in the end game now, Tony.
			distPellet = BFSPath.getPath( grid, pc.getLoc(), PacUtils.nearestGoody(pc.getLoc(), grid) ).size();
		}
		int distBlinky;
		int distInky;
		if (blinky.getMode() ==  PacMode.valueOf("FEAR") ){
			distBlinky = 10;
			distInky = 10;	
		} else {
			distBlinky = BFSPath.getPath( grid, blinky.getLoc(), pc.getLoc()  ).size();
			distInky = BFSPath.getPath( grid, blinky.getLoc(), pc.getLoc()  ).size();
		}

		return (WIN_VALUE - distPellet) + (LOSS_VALUE + distBlinky + distInky) + (PacUtils.numFood( grid ) * -1) / 2 ;
	}


}