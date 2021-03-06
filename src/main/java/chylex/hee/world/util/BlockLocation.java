package chylex.hee.world.util;

public class BlockLocation{
	public final int x,y,z;
	
	public BlockLocation(int x, int y, int z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof BlockLocation){
			BlockLocation loc = (BlockLocation)o;
			return x == loc.x && y == loc.y && z == loc.z;
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		return y+31*x+961*z;
	}
}
