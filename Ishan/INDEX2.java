package Ishan;
import HAL.Util;
import HAL.Tools.FileIO;
import HAL.GridsAndAgents.AgentGrid2D;
import HAL.GridsAndAgents.AgentSQ2Dunstackable;
import HAL.Gui.GridWindow;
import HAL.Gui.UIGrid;
import HAL.Tools.FileIO;
import HAL.Rand;
import HAL.Util;


public class INDEX2 {
    public static void main(String[]args){
        FileIO n=null;
        n=new FileIO("index2","w");
        n.Write("index" + "\n");
        for (int ticky = 0; ticky < 6000; ticky++) {
        int[] innndex = {ticky};
            n.Write(Util.ArrToString(innndex,",")+"\n");
        }
        n.Close();
}
}