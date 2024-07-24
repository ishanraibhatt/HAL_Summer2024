package Ishan;

import HAL.GridsAndAgents.AgentGrid2D;
import HAL.GridsAndAgents.AgentSQ2Dunstackable;
import HAL.Gui.GridWindow;
import HAL.Gui.UIGrid;
import HAL.Tools.FileIO;
import HAL.Rand;
import HAL.Util;

/** DIRECTLY MODIFIED FROM MATHONCO/HAL/DIVISIONDEATHMUTATION.JAVA
 * 
 * ADAPTED TO ALLOW FOR GREATER VARIATION IN DIV-PROB AND DIE-PROB
 * 
 * 
 * CODE WRITTEN BY: ISHAN BHATT
 * 
 * LAST UPDATED: 7/11/2024 
*/

class Cell extends AgentSQ2Dunstackable<Fitness2> {
    double DIV_PROB = 0.0;
    double DIE_PROB = 0.0;
    int color;
    
     
    void Mutate(){
        double Mutates = G.rn.Double();
        double useful = G.rn.Double();
        if(Mutates < G.MUT_PROB && useful > 0.95) {
            //if it mutates, and the mutation actually does something, decide which mutation it will recieve
            if(G.rn.Double() > 0.5) {
                this.DIV_PROB = this.DIV_PROB + 0.01;
                this.DIE_PROB = this.DIE_PROB +0.003;
            }else{
                this.DIV_PROB = this.DIV_PROB -0.01;
                this.DIE_PROB = this.DIE_PROB - 0.003;
            }
        Draw(this.DIV_PROB);
        }
    }

    void Draw(double DIV_PROB){
        if(this.DIV_PROB > 0.2){
            G.vis.SetPix(Isq(),Util.RED);
        }else if(this.DIV_PROB < 0.2){
            G.vis.SetPix(Isq(),Util.BLUE);
        } else {
            G.vis.SetPix(Isq(), Util.GREEN);
        }
    }
    


    void Divide(){
        
        int nOpts=MapEmptyHood(G.hood);//finds von neumann neighborhood indices around cell.
        if(nOpts>0 && G.rn.Double() < DIV_PROB){
            int iDaughter= G.hood[G.rn.Int(nOpts)];
            Cell daughter= G.NewAgentSQ(iDaughter);//generate a daughter, the other is technically the original cell
            daughter.DIV_PROB = this.DIV_PROB;//start both daughters with the same birth and death probabilities.
            daughter.DIE_PROB = this.DIE_PROB;
            daughter.Draw(daughter.DIV_PROB);
            Mutate();//during division, there is a possibility of mutation of one or both daughters
            daughter.Mutate();
        }
    }
}



public class Fitness2 extends AgentGrid2D<Cell> {
    
    int nNormal = 0;
    int nFast = 0;
    int nSlow = 0;
    int nCells = 0;
    int[] CellCounts = new int[]{nCells,nNormal,nFast,nSlow};
    double MUT_PROB = 0.06;
    final static int BLACK= Util.RGB(0,0,0);
    int[]hood=Util.GenHood2D(new int[]{1,0,-1,0,0,1,0,-1}); //equivalent to int[]hood=Util.VonNeumannHood(false);
    Rand rn=new Rand();
    UIGrid vis;
    static FileIO outputFile=null;
    public Fitness2(int x, int y, UIGrid vis, String outputFileName) {
        super(x, y, Cell.class);
        this.vis=vis;
        outputFile=new FileIO(outputFileName,"w");
    }
    public void InitTumor(double radius){
        int[]circleHood= Util.CircleHood(true,radius);//generate circle neighborhood [x1,y1,x2,y2,...]
        int len=MapHood(circleHood,xDim/2,yDim/2);
        for (int i = 0; i < len; i++) {
            Cell c=NewAgentSQ(circleHood[i]);
            c.DIV_PROB=0.2;
            c.DIE_PROB=0.01;
            c.Draw(c.DIV_PROB);
        }
    }


    public void StepCells(int tick){
        nCells = 0;
        nFast = 0;
        nSlow = 0;
        nNormal = 0;
        for (Cell c : this) {//iterate over all cells in the grid
            nCells++;
            if(rn.Double()< c.DIE_PROB){
                vis.SetPix(c.Isq(),BLACK);
                c.Dispose();//removes cell from sptial grid and iteration
            }
            else if(rn.Double()< c.DIV_PROB){
                c.Divide();
            }
            if(c.DIV_PROB == 0.2){
                nNormal++;
            }else if (c.DIV_PROB > 0.2){
                nFast++;
            }else if (c.DIV_PROB < 0.2){
                nSlow++;
            }
        }
        int[] CellCounts = {nCells, nNormal, nFast, nSlow}; 
        outputFile.Write(Util.ArrToString(CellCounts,",")+"\n");
        ShuffleAgents(rn);//shuffles order of for loop iteration
        CleanAgents();
//        IncTick();//increments timestep, including newly generated cells in the next round of iteration
    }

    public static void main(String[]args){
        //ArrayList<Double[]>out=new ArrayList<>();
        int x=500,y=500,scaleFactor=2;
        //int x=1000,y=1000,scaleFactor=1;
        GridWindow vis=new GridWindow(x,y,scaleFactor);//used for visualization
        Fitness2 grid=new Fitness2(x,y,vis, "DTrial10");
        outputFile.Write("Total Cells, Normal Cells, Fast Cells, Slow Cells" + "\n");
        grid.InitTumor(5);
        for (int tick = 0; tick < 5000; tick++) {
            vis.TickPause(0);//set to nonzero value to cap tick rate.
            grid.StepCells(tick);
        }
        outputFile.Close();
        return;
    }
}
    

