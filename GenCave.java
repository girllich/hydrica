package calumny;
import calumny.BitsWithProbability;
import calumny.MersenneTwisterRNG;

public final class GenCave {
    //thanks to http://onjava.com/onjava/2005/02/02/examples/bits_article.java
	//thanks to http://onjava.com/onjava/2005/02/02/bitsets.html
	//thanks to http://d.hatena.ne.jp/tosik/20071115/1195120024
	//thanks to http://github.com/tosik/BitboardCA/blob/master/src/OuterTotalisticCA.cpp
	//thanks to http://stackoverflow.com/questions/2075912/generate-a-random-binary-number-with-a-variable-proportion-of-1-bits
	public final long[][][] board;
    public static interface evaluator {
	public long eval(long s0,long s1,long s2,long s3,long s4,long s5,long s6,long s7,long s8,long s9);
    }
    public static interface noisegen {
	public long noise(int x,int y,int z);
	public long noise(int x,int y,int z, float density);
    } //https://uncommons-maths.dev.java.net/ http://www.honeylocust.com/RngPack/
    public static final evaluator widen=new evaluator() {
    	public final long 
    	eval(long s0,long s1,long s2,long s3,long s4,long s5,long s6,long s7,long s8,long s9)
    	{ return s0 | s1 | s6 | s7 | s8 | s9; };
    };
    public static final evaluator narrow=new evaluator() {
    	public final long 
    	eval(long s0,long s1,long s2,long s3,long s4,long s5,long s6,long s7,long s8,long s9)
    	{ return s5 | s6 | s7 | s8 | s9; };
    };
    public static final evaluator darken=new evaluator() {
    	public final long 
    	eval(long s0,long s1,long s2,long s3,long s4,long s5,long s6,long s7,long s8,long s9)
    	{ return s6 | s7 | s8 | s9; };
    };
    //public static final class randomgen {
   // 	public static ByteBuffer bb=ByteBuffer.wrap(new byte[16]);
    	public static java.util.Random getRandom(int x,int y,int z)
    	{	
		int[] bb= new int[4];
		bb[0] = x;
		bb[1] = y;
		bb[2] = z;
    		return new MersenneTwisterRNG(bb);
    		//return new AESCounterRNG(bb.array());
     	}
    //}
    //public static final randomgen rgen=new randomgen();
    
    public static final class noisegenimpl implements noisegen {
    	public int offsetx,offsety,offsetz;
    	public float density;
    	public noisegenimpl(int offsetx,int offsety, int offsetz)
    	{
    		this.offsetx=offsetx; this.offsety=offsety; this.offsetz=offsetz;
    		this.density=0.45f;
    	}    	
    	public long noise(int x, int y, int z, float density){
    		return new BitsWithProbability(density,GenCave.getRandom(x+offsetx, y+offsety, z+offsetz)).nextLong();
    	}
    	public long noise(int x, int y, int z){
    		return new BitsWithProbability(this.density,GenCave.getRandom(x+offsetx, y+offsety, z+offsetz)).nextLong();
    	}
    }

	//public GenCave(int x, int y)
    //{
	//	board = new long[2][x][y];
	//	vs=null;
    //}
	public int returnx,returny,inset;
	public GenCave(int x, int y, int z, int inset)
    {
		this.inset=(z-1)*2+2;
		this.returnx=(x-2)*8-this.inset;
		this.returny=(y-2)*8-this.inset;
		board = new long[z][x][y];
    }
	public void initnoise2(int level,noisegen r) 
	{
	    for(int x=1;x<(board[level].length-1);x++)
	    	for(int y=1;y<(board[level][0].length-1);y++)
	    		{
	    			board[level][x][y]=r.noise(x,y,level);
	    		}
    } 
	public void darknoise2(int level, noisegen r, float density)
	{
		for(int x=1;x<(board[level].length-1);x++)
	    	for(int y=1;y<(board[level][0].length-1);y++)
	    		{
	    			board[level][x][y]&=~r.noise(x, y, level, density);	    		
	    		}
	}
    public void initnoise(int level,java.util.Random r, int p) 
    { //http://stackoverflow.com/questions/2075912/generate-a-random-binary-number-with-a-variable-proportion-of-1-bits
	for (int j = 8; j < 72; j++)
	    for (int i = 8; i < 72; i++)
		writebit(level, i, j, r.nextInt(100)<p);
    } 
    public void darknoise(int level,java.util.Random r, int p)
    {
	for (int j = 8; j < 72; j++)
	    for (int i = 8; i < 72; i++)
		if(r.nextInt(100)<p)
		    writebit(level, i, j, false);
    }
    public final boolean getbit(int x,int y,int level)
    {    	
    	return ((board[level][x>>>3][y>>>3]>>>((7l-x%8l)+8l*(7l-y%8l)))&1l)>0;
    }
    public void copy(int oldlevel,int newlevel)
    {
    	for (int j = 1; j < (board[0].length-1); j++)
    		for (int i = 1; i < (board[0].length-1); i++)
    		   {board[newlevel][i][j]=board[oldlevel][i][j];}    	
    }
    public void dupmatrix(float[] r, int inset) 
    {
    	for (int level = 0; level < (board.length-1); level++)
    		for (int j = 0; j < returnx; j++)
    			for (int i = 0; i < returny; i++)
    			{
    				if( getbit(i+inset,j+inset,level) )
    					r[i+j*returnx+level*returnx*returny]=1f;
    				else	
    					r[i+j*returny+level*returnx*returny]=0f;
    			}	
    }
    
    public void bitprint(int level)
	{
		System.out.println("Board level: "+level);
		for(int x=1;x<(board[level].length-1);x++)
			for(long z=7l;z>=0l;z--){  
				for(int y=1;y<(board[level][0].length-1);y++)
				{long i=(board[level][y][x]>>>(z*8l))&0xFFl;
				for(long w=7l;w>=0l;w--)
					if(((i>>>w)&0x01l)!=0)
						System.out.print(" ");
					else
						System.out.print("#");
				} System.out.println("");}              
	}
	public void writebit(int level, int x, int y, boolean b)
	{
	    //if(
	    //	                (x>>>3)<1 || 
	    //			(x>>>3)>(board[level].length-2) || 
	    //			(y>>>3)<1 || 
	    //			(y>>>3)>(board[level].length-2))
	    //		return;
		if(b)
			board[level][x>>>3][y>>>3]|=1l<<((7-x%8l)+8l*(7-y%8l));
		else
			board[level][x>>>3][y>>>3]&=~(1l<<((7-x%8l)+8l*(7-y%8l)));
	}

    public final void step(int level, int newlevel, evaluator e)
	{
		for(int x=1;x<(board[level].length-1);x++)
			for(int y=1;y<(board[level][0].length-1);y++)
			{
			    long s0, s1, s2, s3, s4, s5, s6, s7, s8, s9;
				long c, cnw, cn, cne, cw, ce, csw, cs, cse;
				c = board[level][x][y];
				cnw = ((board[level][x][y] & 0xfefefefefefefe00l) >>> 9)
						| ((board[level][x - 1][y - 1] & 0x0000000000000001l) << 63)
						| ((board[level][x][y - 1] & 0x00000000000000fel) << 55)
						| ((board[level][x - 1][y] & 0x0101010101010100l) >>> 1);
				cn = ((board[level][x][y] & 0xffffffffffffff00l) >>> 8)
						| ((board[level][x][y - 1] & 0x00000000000000ffl) << 56);
				cne = ((board[level][x][y] & 0x7f7f7f7f7f7f7f00l) >>> 7)
						| ((board[level][x + 1][y - 1] & 0x0000000000000080l) << 49)
						| ((board[level][x][y - 1] & 0x000000000000007fl) << 57)
						| ((board[level][x + 1][y] & 0x8080808080808000l) >>> 15);
				cw = ((board[level][x][y] & 0xfefefefefefefefel) >>> 1)
						| ((board[level][x - 1][y] & 0x0101010101010101l) << 7);
				ce = ((board[level][x][y] & 0x7f7f7f7f7f7f7f7fl) << 1)
						| ((board[level][x + 1][y] & 0x8080808080808080l) >>> 7);
				csw = ((board[level][x][y] & 0x00fefefefefefefel) << 7)
						| ((board[level][x - 1][y + 1] & 0x0100000000000000l) >>> 49)
						| ((board[level][x][y + 1] & 0xfe00000000000000l) >>> 57)
						| ((board[level][x - 1][y] & 0x0001010101010101l) << 15);
				cs = ((board[level][x][y] & 0x00ffffffffffffffl) << 8)
						| ((board[level][x][y + 1] & 0xff00000000000000l) >>> 56);
				cse = ((board[level][x][y] & 0x007f7f7f7f7f7f7fl) << 9)
						| ((board[level][x + 1][y + 1] & 0x8000000000000000l) >>> 63)
						| ((board[level][x][y + 1] & 0x7f00000000000000l) >>> 55)
						| ((board[level][x + 1][y] & 0x0080808080808080l) << 1);

				s0 = ~(cnw | cn);
				s1 = cnw ^ cn;
				s2 = cnw & cn;

				s3 = cne & s2;
				s2 = (s2 & ~cne) | (s1 & cne);
				s1 = (s1 & ~cne) | (s0 & cne);
				s0 = s0 & ~cne;

				s4 = cw & s3;
				s3 = (s3 & ~cw) | (s2 & cw);
				s2 = (s2 & ~cw) | (s1 & cw);
				s1 = (s1 & ~cw) | (s0 & cw);
				s0 = s0 & ~cw;

				s5 = ce & s4;
				s4 = (s4 & ~ce) | (s3 & ce);
				s3 = (s3 & ~ce) | (s2 & ce);
				s2 = (s2 & ~ce) | (s1 & ce);
				s1 = (s1 & ~ce) | (s0 & ce);
				s0 = s0 & ~ce;

				s6 = csw & s5;
				s5 = (s5 & ~csw) | (s4 & csw);
				s4 = (s4 & ~csw) | (s3 & csw);
				s3 = (s3 & ~csw) | (s2 & csw);
				s2 = (s2 & ~csw) | (s1 & csw);
				s1 = (s1 & ~csw) | (s0 & csw);
				s0 = s0 & ~csw;

				s7 = cs & s6;
				s6 = (s6 & ~cs) | (s5 & cs);
				s5 = (s5 & ~cs) | (s4 & cs);
				s4 = (s4 & ~cs) | (s3 & cs);
				s3 = (s3 & ~cs) | (s2 & cs);
				s2 = (s2 & ~cs) | (s1 & cs);
				s1 = (s1 & ~cs) | (s0 & cs);
				s0 = s0 & ~cs;

				s8 = cse & s7;
				s7 = (s7 & ~cse) | (s6 & cse);
				s6 = (s6 & ~cse) | (s5 & cse);
				s5 = (s5 & ~cse) | (s4 & cse);
				s4 = (s4 & ~cse) | (s3 & cse);
				s3 = (s3 & ~cse) | (s2 & cse);
				s2 = (s2 & ~cse) | (s1 & cse);
				s1 = (s1 & ~cse) | (s0 & cse);
				s0 = s0 & ~cse;

				s9 = c & s8;
				s8 = (s8 & ~c) | (s7 & c);
				s7 = (s7 & ~c) | (s6 & c);
				s6 = (s6 & ~c) | (s5 & c);
				s5 = (s5 & ~c) | (s4 & c);
				s4 = (s4 & ~c) | (s3 & c);
				s3 = (s3 & ~c) | (s2 & c);
				s2 = (s2 & ~c) | (s1 & c);
				s1 = (s1 & ~c) | (s0 & c);
				s0 = s0 & ~c;
				
				board[newlevel][x][y] = e.eval(s0,s1,s2,s3,s4,s5,s6,s7,s8,s9);
				//board[newlevel][x][y] = s6 | s7 | s8 | s9;
				//board[newlevel][x][y] = s0 | s1 | s6 | s7 | s8 | s9;
				//System.out.println(Long.toBinaryString(s0));
				
				//board[newlevel][x][y] = (~board[level][x][y] & (s0 | s1 | s6 | s7 | s8 )) | 
				//    ( board[level][x][y] & (s0 | s5 | s6 | s7 | s8 )) ;
				
				//board[newlevel][x][y] = (~board[level][x][y] & s3)
				//		| (board[level][x][y] & (s2 | s3));

			}
	}
    

    
    public void cave2d(int x,int y )
    {    	
    	noisegen g=new noisegenimpl(x,y,0); //z is leveled differently
	    initnoise2(0,g);
	    step(0,1,widen);
	    step(1,0,widen);
	    step(0,1,widen);
	    step(1,0,widen);
	    step(0,1,narrow);
	    step(1,0,narrow);
	    //bitprint(0);
    }
   

    public void cave3d(int x, int y, int z)
    {
    	noisegen g=new noisegenimpl(x>>3,y>>3,z); //z is leveled differently
	    initnoise2(4,g);
	    //System.err.println(this.toString()+"firstresultdata:"+board[4][1][1]);
	    //c.initnoise(0,ran,45);
	    //c.bitprint(0);
	    step(4,5,widen);
	    step(5,4,widen);
	    step(4,5,widen);
	    step(5,4,widen);
	    step(4,5,narrow);
	    step(5,4,narrow);
	    //System.err.println(this.toString()+"mediumresultdata:"+board[4][1][1]);
	    //copy(4,5);
	    //bitprint(4);
	    int i=0;
	    for(i=4;i<16;i++)
	    	{	    	
     		    //System.err.println("@"+i+"resultdata:"+board[4][1][1]);
	    		copy(i,i+1);
	    		darknoise2(i+1, g, 0.1f);
	    		step(i+1,i+2,darken);
	    		step(i+2,i+1,darken);
	    		//bitprint(i+1);
	    	}
	    //System.err.println("blortresultdata:"+board[4][1][1]);
	    for(i=4;i>1;i--)
    	{	    	
    		copy(i,i-1);
    		darknoise2(i-1, g, 0.4f);
    		step(i-1,i-2,darken);
    		step(i-2,i-1,darken);
    		//bitprint(i-1);
    	}
	    //System.err.println(this.toString()+"finalresultdata:"+board[4][1][1]);
	    //VolumetricSpaceArray vsa=new VolumetricSpaceArray(new Vec3D(1,1,1),returnx,returny,24);
	    //vsa.getData()[117]=1f;
	    //dupmatrix(vsa.getData(),20);
	    //vsa.closeSides();
	    //ArrayIsoSurface izo=new ArrayIsoSurface(vsa);
	    //WETriangleMesh we=new WETriangleMesh();
	    //izo.computeSurfaceMesh(we, 0.5f);
	    
	    //return null;
    }

    public void printint()
    {
    	System.out.println("BEGIN");
    		for (int j = 0; j < returnx; j++){
    			for (int i = 0; i < returny; i++)
    				if( getbit(i+inset,j+inset,0) )
    					System.out.print("#");
    				else	
    					System.out.print(" ");System.out.println("");}
    		System.out.println("END");
    }
	public static void main(String [] args)
	{
	    GenCave c=new GenCave(12,12,26,0);//8x8 (+2) 
	    c.cave3d(0, 0, 0);
	    //c.cave3d(0, 0, 0);
	    c.bitprint(4);
	    //c.cave3d(8, 0, 0);
	    //c.bitprint(4);
	    //c.cave3d(0, 0, 0);
	    //c.bitprint()
	    //c.printint();
	    //c.cave2d(0, 1);
	    //c.printint();
	    //c.cave2d(0, 2);
	    //c.printint();
	    //java.util.Random ran=new java.util.Random(1);
//	    noisegen g=new noisegenimpl(0,0,0); //z is leveled differently
//	    c.initnoise2(0,g);
//	    //c.initnoise(0,ran,45);
//	    //c.bitprint(0);
//	    c.step(0,1,widen);
//	    c.step(1,0,widen);
//	    c.step(0,1,widen);
//	    c.step(1,0,widen);
//	    c.step(0,1,narrow);
//	    c.step(1,0,narrow);
//	    c.copy(0,1);
//	    c.bitprint(0);
//	    for(int i=0;i<11;i++)
//	    	{	    	
//	    		c.copy(i,i+1);
//	    		c.darknoise2(i+1, g, 0.1f);
//	    		c.step(i+1,i+2,darken);
//	    		c.step(i+2,i+1,darken);
//	    		c.bitprint(i+1);
//	    	}
	}

}
