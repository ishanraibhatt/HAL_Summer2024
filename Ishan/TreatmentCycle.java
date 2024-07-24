package Ishan;

import HAL.GridsAndAgents.AgentGrid2D;
import HAL.GridsAndAgents.AgentSQ2Dunstackable;
import HAL.Gui.GridWindow;
import HAL.Gui.UIGrid;
import HAL.Tools.FileIO;
import HAL.Rand;
import HAL.Util;

class CancerCell extends AgentSQ2Dunstackable<TreatmentCycle> {
    double DIV_PROB = 0.2;
    double DIE_PROB = 0.01;
    double T1_RESISTANCE = 0.0;
    double T2_RESISTANCE = 0.0;
    int color;
     
    void Mutate(){
        double Mutates = G.rn.Double();
        double useful = G.rn.Double();
        if(Mutates < G.MUT_PROB && useful > 0.95) {
            //if it mutates, and the mutation actually does something, decide which mutation it will recieve
            if(G.rn.Double() > 0.5) {
                this.T1_RESISTANCE = this.T1_RESISTANCE + 0.01;
                this.T2_RESISTANCE = this.T2_RESISTANCE -0.01;
            }else{
                this.T1_RESISTANCE = this.T1_RESISTANCE -0.01;
                this.T2_RESISTANCE = this.T2_RESISTANCE + 0.01;
            }
        Draw(this.T1_RESISTANCE);
        }
    }

    void Draw(double T1_RESISTANCE){
        
        if(this.T1_RESISTANCE > 0.2){
            //Super Resistance to T1, Super Weak to T2
            G.vis.SetPix(Isq(),Util.RED);
        }else if(this.T1_RESISTANCE > 0.0){
            //Slightly Resistant to T1, Slightly Weak to T2
            G.vis.SetPix(Isq(),Util.YELLOW);
        }else if(this.T1_RESISTANCE < -0.2){
            //Slightly Resistant to T2, Slightly Weak to T1
            G.vis.SetPix(Isq(),Util.BLUE);
        }else if(this.T1_RESISTANCE < 0.0){
            //Super Resistant to T2, Slightly Weak to T1
            G.vis.SetPix(Isq(),Util.CYAN);
        } else {
            //No resistances to either
            G.vis.SetPix(Isq(),Util.GREEN);
        }
    }
    


    void Divide(){
        
        int nOpts=MapEmptyHood(G.hood);//finds von neumann neighborhood indices around cell.
        if(nOpts>0 && G.rn.Double() < DIV_PROB){
            int iDaughter= G.hood[G.rn.Int(nOpts)];
            CancerCell daughter= G.NewAgentSQ(iDaughter);//generate a daughter, the other is technically the original cell
            daughter.DIV_PROB = this.DIV_PROB;//start both daughters with the same birth and death probabilities.
            daughter.DIE_PROB = this.DIE_PROB;
            daughter.T1_RESISTANCE = this.T1_RESISTANCE;
            daughter.T2_RESISTANCE = this.T2_RESISTANCE;
            daughter.Draw(daughter.T1_RESISTANCE);
            Mutate();//during division, there is a possibility of mutation of one or both daughters
            daughter.Mutate();
        }
    }
}


public class TreatmentCycle extends AgentGrid2D<CancerCell> {
    int T1Res = 0;
    int T1SemiRes = 0;
    int T2SemiRes = 0;
    int T2Res = 0;
    int Normal = 0;
    int Total = 0;
    int[] CellCounts = new int[]{Total,Normal,T1Res,T1SemiRes,T2Res,T2SemiRes};
    double MUT_PROB = 0.06;
    final static int BLACK= Util.RGB(0,0,0);
    int[]hood=Util.GenHood2D(new int[]{1,0,-1,0,0,1,0,-1}); //equivalent to int[]hood=Util.VonNeumannHood(false);
    Rand rn=new Rand();
    UIGrid vis;
    static FileIO outputFile=null;
    public TreatmentCycle(int x, int y, UIGrid vis, String outputFileName) {
        super(x, y, CancerCell.class);
        this.vis=vis;
        outputFile=new FileIO(outputFileName,"w");
    }
    public void InitTumor(double radius){
        int[]circleHood= Util.CircleHood(true,radius);//generate circle neighborhood [x1,y1,x2,y2,...]
        int len=MapHood(circleHood,xDim/2,yDim/2);
        for (int i = 0; i < len; i++) {
            CancerCell c=NewAgentSQ(circleHood[i]);
            c.T1_RESISTANCE = 0.0;
            c.T2_RESISTANCE = 0.0;
            c.DIV_PROB=0.2;
            c.DIE_PROB=0.01;
            c.Draw(c.T1_RESISTANCE);
        }
    }
    public void InitBloodVessel(){

    }


    public void StepCells(int tick){
        int T1Res = 0;
        int T1SemiRes = 0;
        int T2SemiRes = 0;
        int T2Res = 0;
        int Normal = 0;
        int Total = 0;
        for (CancerCell c : this) {//iterate over all cells in the grid
            Total++;
            if(tick < 1500 || (tick > 1515 && tick < 3000) || (tick > 3015 && tick < 4500) || tick > 4515){
                //for normal w/o treatment
                if(rn.Double()< c.DIE_PROB){
                    vis.SetPix(c.Isq(),BLACK);
                    c.Dispose();//removes cell from sptial grid and iteration
                }else if(rn.Double()< c.DIV_PROB){
                    c.Divide();
                }
            }else if((tick >= 1500 && tick <= 1515) || (tick >= 3000 && tick <= 3015)){
                //for treatment1.
                if(rn.Double()< ((c.DIE_PROB)+ 0.2 - (8*c.T1_RESISTANCE))){
                    vis.SetPix(c.Isq(),BLACK);
                    c.Dispose();//removes cell from sptial grid and iteration
                }else if(rn.Double()< c.DIV_PROB){
                    c.Divide();
                }
            }else if((tick >= 4500 && tick <= 4515)){
                //for treatment2
                if(rn.Double()< ((c.DIE_PROB)+ 0.2 - (10*c.T2_RESISTANCE))){
                    vis.SetPix(c.Isq(),BLACK);
                    c.Dispose();//removes cell from sptial grid and iteration
                }else if(rn.Double()< c.DIV_PROB){
                    c.Divide();
                }
            }

            if(c.T1_RESISTANCE == 0.0){
                Normal++;
            }else if (c.T1_RESISTANCE > 0.2){
                T1Res++;
            }else if (c.T1_RESISTANCE > 0.0){
                T1SemiRes++;
            }else if (c.T1_RESISTANCE < -0.2){
                T2Res++;
            }else if(c.T1_RESISTANCE < 0.0){
                T2SemiRes++;
            }
        }
        int[] CellCounts = {Total,Normal,T1Res,T1SemiRes,T2Res,T2SemiRes};
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
        TreatmentCycle grid=new TreatmentCycle(x,y,vis, "JTrial10");
        outputFile.Write("Total Cells,Normal Cells,T1 Resistant,T1 Semi Resistant,T2 Resistant,T2 Semi Resistant" + "\n");
        grid.InitTumor(5);
        for (int tick = 0; tick < 6000; tick++) {
            vis.TickPause(0);//set to nonzero value to cap tick rate.
            //from ticks 0-2000 nothing, from 2000-3000 T1, from 3000-4000 nothing, from 4000-5000 T1, then from 5000-6000 nothing
            grid.StepCells(tick);
        }
        outputFile.Close();
        return;
    }
}
