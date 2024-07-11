package Ishan;

import HAL.GridsAndAgents.AgentSQ2Dunstackable;
import HAL.Gui.GridWindow;
import HAL.GridsAndAgents.AgentGrid2D;
import HAL.Rand;
import HAL.Util;

import static HAL.Util.*;

class Cell extends AgentSQ2Dunstackable<test> {
    int color;


    public void Step(double DEATH_PROB, double BIRTH_PROB) {
        //mutation section
        Double Mutated = G.rn.Double();
        Double MutatedUsefull = G.rn.Double();
         if (Mutated < G.MUTATE_PROB && MutatedUsefull < 0.95) {
            DEATH_PROB = DEATH_PROB + 0.00;
            BIRTH_PROB = BIRTH_PROB + 0.00;
            
         }
         if (Mutated < G.MUTATE_PROB && MutatedUsefull > 0.95) {
             //mutation actually does something
             if(G.rn.Double() < 0.5) {
                DEATH_PROB = DEATH_PROB + 0.002;
                BIRTH_PROB = BIRTH_PROB + 0.02;
                
             }else {
                DEATH_PROB = DEATH_PROB - 0.002;
                BIRTH_PROB = BIRTH_PROB - 0.02;
             }
         }
         //coloration section
         if(BIRTH_PROB == 0.2) {
            color = Util.GREEN;
         }else if(BIRTH_PROB > 0.2) {
            color = Util.MAGENTA;
         }else if(BIRTH_PROB < 0.2){
            color = Util.YELLOW;
         }

         // divide or die section
        if (G.rn.Double() < DEATH_PROB) {
            Dispose();
            return;
        }
        if (G.rn.Double() < BIRTH_PROB) {
            int nOptions = G.MapEmptyHood(G.mooreHood, Xsq(), Ysq());
            if(nOptions>0) {
                G.NewAgentSQ(G.mooreHood[G.rn.Int(nOptions)]).color=color;

            }
        }
        return;
    }
}

public class test extends AgentGrid2D<Cell> {
    int BLACK=RGB(0,0,0);
    double DEATH_PROB=0.01;
    double BIRTH_PROB=0.2;
    double MUTATE_PROB=0.06;
    Rand rn=new Rand();
    int[]mooreHood=MooreHood(false);
    int color;
    public test(int x, int y,int color) {
        super(x, y, Cell.class);
        this.color=color;
    }
    public void Setup(int radx, int rady){
        int[]coords= RectangleHood(true,radx,rady);
        int nCoords= MapHood(coords,xDim/2,yDim/2);
        for (int i = 0; i < nCoords ; i++) {
            NewAgentSQ(coords[i]).color=color;
        }
    }
    public void Step() {
        for (Cell c : this) {
            //c.Step(DEATH_PROB, BIRTH_PROB);
        }
        CleanAgents();
        ShuffleAgents(rn);
    }
    public void Draw(GridWindow vis){
        for (int i = 0; i < vis.length; i++) {
            Cell c = GetAgent(i);
            vis.SetPix(i, c == null ? BLACK : c.color);
        }
    }

    public static void main(String[] args) {
        test t=new test(100,100, Util.GREEN);
        GridWindow win=new GridWindow(100,100,10);
        t.Setup(10,10);
        for (int i = 0; i < 100000; i++) {
            win.TickPause(10);
            t.Step();
            t.Draw(win);
        }
    }
}
