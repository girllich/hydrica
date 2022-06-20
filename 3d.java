package calumny;
import java.io.IOException;
import java.nio.ByteBuffer;
import toxi.geom.*;
import toxi.geom.mesh.*;
import toxi.volume.*;

public final class
3d {
  public final VolumetricSpaceArray vs;
  public setup(int returnx, int returny, int z) {
    vs = new VolumetricSpaceArray(new Vec3D(1, 1, 1), returnx, returny, z);
  }
  public final LaplacianSmooth sm = new LaplacianSmooth();
  {
    try {
      dupmatrix(this.vs.getData(), 20);
      // this.vs.closeSides();
      // System.err.println("lastresultdata:"+board[4][1][1]);
      ArrayIsoSurface iso = new ArrayIsoSurface(this.vs);
      WETriangleMesh we = new WETriangleMesh();
      // iso.computeSurfaceMesh(null, 0.5f);
      WETriangleMesh we2 = (WETriangleMesh)iso.computeSurfaceMesh(we, 0.5f);
      // we2.computeVertexNormals();
      sm.filter(we2, 1);
      // we2.saveAsOBJ("/tmp/c.obj");
      // if(true)
      //{
      float[] f;
      if (!normals)
        f = we2.getMeshAsVertexArray(null, 0, 3);
      else {
        // we2.computeVertexNormals();
        f = we2.getVertexNormalsAsArray(we2.getMeshAsVertexArray(null, 0, 6), 3,
                                        6);
      }

      java.io.DataOutputStream d = new java.io.DataOutputStream(stream);
      // System.err.print("First three floats are"+f[0]+","+f[1]+","+f[2]);
      for (int i = 0; i < f.length; i++) {
        // System.err.print((int) ((f[i]+0.5f)*256.0f));
        if ((i % 6) > 2)
          d.write((int)((f[i] + 1.0f) * 128.0f));
        // else
        // if(Math.abs(f[i])>0.5) System.err.println("Error"+f[i]);
        else
          d.write((int)((f[i] + 0.5f) * 256.0f));
      }
      //} else {
      //	we2.saveAsOBJ(stream);

      //}
    } catch (IOException e) {
      e.printStackTrace();
    }
    // ByteBuffer g=ByteBuffer.wrap(new byte[f.length*4]);
    // g.asFloatBuffer().put(f);
    // return g.array();
  }
}
