package jforex.history;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import java.io.*;

import com.dukascopy.api.*;

/**
 * The following strategy retrieve history ticks by:
 * - shift
 * - time interval
 * 
 * The strategy logs the results such that user can check that 
 * the corresponding ones match by using different approaches.
 * 
 * For the sake of atomicity, each test on purpose uses 
 * as little as possible global variables.
 *
 */
@SuppressWarnings("serial")
@RequiresFullAccess
public class TogetSashasFile implements IStrategy {
    
    private IHistory history;
    private IConsole console;
    
    //
    //MR-> adding for definition of DF filter coefficients
   static  double DFm15[] = { 0.06160206, 0.06138969, 0.06096543, 0.06033228, 0.05949463, 0.05845839, 0.05723027, 0.05582001, 0.05423432, 0.05248615, 0.05058605, 0.04854683, 0.04638147, 0.04410408, 0.04172914, 0.03927236, 0.03674915, 0.03417404, 0.03156302, 0.02893255, 0.02629824, 0.02367437, 0.02107783, 0.01851940, 0.01601517, 0.01357747, 0.01121907, 0.00895081, 0.00678294, 0.00472446, 0.00278405, 0.00096894, -0.00071624, -0.00226668, -0.00367728, -0.00494545, -0.00607250, -0.00705454, -0.00789681, -0.00859860, -0.00916332, -0.00959454, -0.00989805, -0.01008011, -0.01014734, -0.01010584, -0.00996220, -0.00972579, -0.00940600, -0.00900952, -0.00854392, -0.00802472, -0.00745394, -0.00684577, -0.00620580, -0.00554267, -0.00486452, -0.00418056, -0.00349919, -0.00282748, -0.00217060, -0.00153293, -0.00092189, -0.00034597, 0.00019314, 0.00070086, 0.00115844, 0.00157841, 0.00195195, 0.00228067, 0.00256313, 0.00279849, 0.00298586, 0.00312633, 0.00322352, 0.00328303, 0.00331063, 0.00330635, 0.00326188, 0.00317506, 0.00310127, 0.00298040, 0.00285581, 0.00271984, 0.00257965, 0.00244069, 0.00230898, 0.00219051, 0.00209219, 0.00202189, 0.00198787, 0.00199966, 0.00206798, 0.00220566, -0.00981400 };
   static  double DFm30[] = { 0.12308114, 0.12138303, 0.11803487, 0.11312485, 0.10678887, 0.09918866, 0.09052840, 0.08103240, 0.07094065, 0.06050288, 0.04996605, 0.03957690, 0.02956613, 0.02012983, 0.01146221, 0.00369983, -0.00303898, -0.00868022, -0.01318509, -0.01655097, -0.01881253, -0.02003063, -0.02029728, -0.01972189, -0.01843477, -0.01658082, -0.01430672, -0.01175303, -0.00904320, -0.00630515, -0.00369358, -0.00124269, 0.00092626, 0.00277697, 0.00426926, 0.00538852, 0.00613948, 0.00654180, 0.00663281, 0.00645954, 0.00608070, 0.00556313, 0.00497955, 0.00441242, 0.00395102, 0.00369892, 0.00378072, -0.00893025 };
   static  double DFh1[] = { 0.24470986, 0.23139774, 0.20613797, 0.17166230, 0.13146908, 0.08950388, 0.04960092, 0.01502271, -0.01188034, -0.02989874, -0.03898967, -0.04014114, -0.03511968, -0.02611614, -0.01539057, -0.00495354, 0.00368589, 0.00963614, 0.01265139, 0.01307496, 0.01169702, 0.00974842, 0.00898900, -0.00649746 };
    int length=96; //length of input vector
    int VolLength=500; //Volatility averaging period
    List<IBar> bars; //list of downloaded bars
    int VolatilityStop;
    int VolatilityTrail; 
     
    @Override
    public void onStart(IContext context) throws JFException {
        
        history = context.getHistory();
        console = context.getConsole();   

        console.getOut().println("I started!!!!");  
       // getTickByShift();
        getTicksByTimeInterval();

    }
    
    

      
    private void getTickByShift() throws JFException{  
        ITick tick0 = history.getTick(Instrument.EURUSD, 0);
        ITick tick1 = history.getTick(Instrument.EURUSD, 1);
        console.getOut().println(String.format("last tick: %s; previous tick: %s", tick0, tick1));
        console.getOut().println("tik0 "+tick0+" volume "+tick0.getTotalAskVolume()+ "   best asks"+tick0.getAsks()[0]+" "+  tick0.getAsks()[1] + " "+ tick0.getAsks()[2]  );        
        console.getOut().println("tik0 "+tick0+" volume "+tick0.getAskVolume());
    }
      
    private void getTicksByTimeInterval() throws JFException{            
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS") {{setTimeZone(TimeZone.getTimeZone("GMT"));}};
        ITick lastTick = history.getLastTick(Instrument.EURUSD);
       // List<ITick> ticks = history.getTicks(Instrument.EURUSD, lastTick.getTime() - 1*24*60*60*1000, lastTick.getTime());
       List<ITick> ticks = history.getTicks(Instrument.EURUSD, lastTick.getTime() - 10*1000, lastTick.getTime());
        long starttime,finishtime;
        starttime= lastTick.getTime() - 5*24*60*60*1000;
        finishtime=lastTick.getTime();
        console.getOut().println("starttime=" +starttime + "   finishtime= " + finishtime  );
         
        int last = ticks.size() - 1;
        console.getOut().println(String.format(
            "Tick count=%s; Latest bid price=%.5f, time=%s; Oldest bid price=%.5f, time=%s", 
            ticks.size(), ticks.get(last).getBid(), sdf.format(ticks.get(last).getTime()), ticks.get(0).getBid(), sdf.format(ticks.get(0).getTime())));
        
                
        console.getOut().println(String.format(
            "Latest ask price=%.5f, time=%s; Oldest ask price=%.5f, time=%s", 
            ticks.get(last).getAsk(), sdf.format(ticks.get(last).getTime()), ticks.get(0).getAsk(), sdf.format(ticks.get(0).getTime())));   
        
        int i = 0; 
      /*  while (i <= last ){
          console.getOut().println("tick["+i+"] -> "+ticks.get(i) );
            i++;
        }   
        */
        
         Period periodbar=Period.FIFTEEN_MINS;
       //Period periodbar=Period.THIRTY_MINS;
        // Period periodbar=Period.ONE_HOUR;  
         long prevBarTime = history.getPreviousBarStart(periodbar, history.getLastTick(Instrument.EURUSD).getTime());
         long startTime =  history.getTimeForNBarsBack(periodbar, prevBarTime, 25000);  //21600
        // final  List<IBar> bars = history.getBars(Instrument.EURUSD, periodbar, OfferSide.BID, startTime, prevBarTime);
        // final  List<IBar> bars = history.getBars(Instrument.EURUSD, periodbar, OfferSide.BID, Filter.WEEKENDS, startTime, prevBarTime);
                             bars = history.getBars(Instrument.EURUSD, periodbar, OfferSide.BID, Filter.WEEKENDS, startTime, prevBarTime);
                             
         int lastBar = bars.size() - 1;
    console.getOut().println(String.format(
    "Previous bar close price=%.5f; 4th to previous bar close price=%.5f",
    bars.get(lastBar).getClose(), bars.get(0).getClose())); 
    
    console.getOut().println("first bar 0 -> " + bars.get(0) );
    console.getOut().println("last bar lastBar ->" + bars.get(lastBar) );
     console.getOut().println("number of bars loaded " + bars.size() ); 
     
 
     
     int FilterSize;
     int period_min=0;
    // FilterSize=7777;
     if (periodbar == Period.FIFTEEN_MINS) { period_min=15;} 
     if (periodbar == Period.THIRTY_MINS) { period_min=30;}
     if (periodbar == Period.ONE_HOUR) { period_min=60;}
     switch ( period_min ) { 
        case 15:
         {FilterSize=DFm15.length; VolatilityStop=24;VolatilityTrail=2;}
        break;
        case 30: 
         {FilterSize=DFm30.length; VolatilityStop=24;VolatilityTrail=2;}
        break;
        case 60:
         {FilterSize=DFh1.length;  VolatilityStop=24;VolatilityTrail=2;}
        break;
        default:
        {VolatilityStop=-1;VolatilityTrail=-1;console.getOut().println("Wrong Bar period!!!" ); return ;}
        }
     
    //  FilterSize=DFm15.length;
      
     
     console.getOut().println("FilterSize = " + FilterSize);
     console.getOut().println("length = " + length);
     console.getOut().println("VolatilityStop = " + VolatilityStop);
     console.getOut().println("VolatilityTrail = " + VolatilityTrail);
     console.getOut().println("Instrument.EURUSD.getPipValue() = " + Instrument.EURUSD.getPipValue());
     
     //int Stop[]=new int[bars.size()];
     //int TrailingStop[]=new int[bars.size()];
     double Stop[]=new double[bars.size()];
     double TrailingStop[]=new double[bars.size()];
     for (int ll = 0; ll <= lastBar; ll++) {Stop[ll]=-666;TrailingStop[ll]=-666;} //making all elements invalid for submitting orders
    // for (int ll = VolLength+VolatilityStop; ll <= lastBar; ll++) {Stop[ll]=(int) Math.floor(NeoVolatility(ll,VolatilityStop));}
    // for (int ll = VolLength+VolatilityTrail; ll <= lastBar; ll++) {TrailingStop[ll]=(int) Math.floor(NeoVolatility(ll,VolatilityTrail));}
    for (int ll = VolLength+VolatilityStop; ll <= lastBar; ll++) {Stop[ll]=NeoVolatility(ll,VolatilityStop);}
    for (int ll = VolLength+VolatilityTrail; ll <= lastBar; ll++) {TrailingStop[ll]=NeoVolatility(ll,VolatilityTrail);}

     
     double DFaveraged[]=new double[bars.size()];
     for (int ll = 0; ll <= lastBar; ll++) {DFaveraged[ll]=0;} //making all elements  zero
     for (int ll = 0+(FilterSize-1); ll <= lastBar-(FilterSize-1); ll++)
      {
      for (int jj = 0; jj <= FilterSize - 1; jj++) 
         {
           
        switch ( period_min ) { 
        case 15:
           DFaveraged[ll]+=DFm15[jj]*1/2*(bars.get(ll+jj).getClose() + bars.get(ll-jj).getClose());
        break;
        case 30:
           DFaveraged[ll]+=DFm30[jj]*1/2*(bars.get(ll+jj).getClose() + bars.get(ll-jj).getClose());
        break;
        case 60:
           DFaveraged[ll]+=DFh1[jj]*1/2*(bars.get(ll+jj).getClose() + bars.get(ll-jj).getClose());
        break;
        default:
        {console.getOut().println("Wrong Bar period!!!" ); return ;}
        }                     
         } 
      }
     
     
    double outputNN[]=new double[bars.size()];
    int sampp=0, sampn=0;
    for (int ll = 0; ll <= lastBar; ll++){outputNN[ll]=-666 ;}  //making non defined elements 666 :-)
    
    for (int ll = VolLength+VolatilityStop; ll <= lastBar-(FilterSize-1)-1; ll++) //starting value because of existence of Stop 
         {
         //if((iCustom(NULL,0,"NeoTech",DI,D,0,l-1)-iCustom(NULL,0,"NeoTech",DI,D,0,l))/Point>=0) 
         if((DFaveraged[ll+1]-DFaveraged[ll])>=0)
           {
           //for (int t=1;t<l;t++)    
           //for (int t=1;t<bars.size()+FilterSize;t++)
           //for (int t=1;t<=FilterSize;t++)
           for (int t=1;t<= lastBar-ll;t++)
           {
           //if ((Close[l-t]-Close[l]) >TrailingStop*Point) {output[l]=1;sampp++;break;}
           if ((bars.get(ll+t).getClose() - bars.get(ll).getClose()) >TrailingStop[ll]*Instrument.EURUSD.getPipValue()) {outputNN[ll]=1.0;sampp++;break;}
           //if ((Close[l-t]-Close[l]) <-1*Stop*Point) {output[l]=-1;sampn++; break;}
           if ((bars.get(ll+t).getClose()-bars.get(ll).getClose()) <-1*Stop[ll]*Instrument.EURUSD.getPipValue()) {outputNN[ll]=-1.0;sampn++;  break;}
           //console.getOut().println("change + to -");
           }
           }
         //if((iCustom(NULL,0,"NeoTech",DI,D,0,l-1)-iCustom(NULL,0,"NeoTech",DI,D,0,l))/Point<0) 
       
         if((DFaveraged[ll+1]-DFaveraged[ll])<0)
           {
           //for (t=1;t<l;t++)    
          // for (int t=1;t<bars.size()+FilterSize;t++)
          // for (int t=1;t<=FilterSize;t++)
          for (int t=1;t<=lastBar-ll;t++)
           {
           //if ((Close[l-t]-Close[l]) <-1*TrailingStop*Point) {output[l]=-1; sampn++;break;}
           if (( bars.get(ll+t).getClose()-bars.get(ll).getClose() ) < -1*TrailingStop[ll]*Instrument.EURUSD.getPipValue()  ) {outputNN[ll]=-1; sampn++;break;}
           //if ((Close[l-t]-Close[l]) >Stop*Point) {output[l]=1;sampp++;break;}
          if ((bars.get(ll+t).getClose()-bars.get(ll).getClose()) >Stop[ll]*Instrument.EURUSD.getPipValue()) {outputNN[ll]=1;sampp++;break;}
           }
           }
        
        }     
     console.getOut().println("trends up=" +  sampp + " trends down=" +  sampn);
       
     
     double inputNN[][]=new double[bars.size()][length];
     double coeff= 1./Math.sqrt(period_min/60.0);
     console.getOut().println("period_min="+period_min+" coeff = " + coeff);
    // double input[]= new double[length];
     
     //for (int kk = 0; kk <= length - 1; kk++){input[kk]=0;} //making non defined elements 0   
     
     for (int ll = 0; ll <= lastBar; ll++){for (int kk = 0; kk <= length - 1; kk++)   inputNN[ll][kk]=0 ;}  //making non defined elements 0  
     

     
     for (int ll = length; ll <= lastBar; ll++)
         { //console.getOut().println("ll = " + ll);
      
      for (int kk = 0; kk <= length - 1; kk++)
       { //  input[k]=coeff* 100./(Close[l+k])*(Close[l+k]-Close[l+k+1]);
        // input[kk]=coeff* 100./(bars.get(ll-kk).getClose())*(bars.get(ll-kk).getClose()-bars.get(ll-kk-1).getClose());
        inputNN[ll][kk] = coeff* 100./(bars.get(ll-kk).getClose())*(bars.get(ll-kk).getClose()-bars.get(ll-kk-1).getClose());
        }        
      //   console.getOut().println(ll+"->  "+ input[0]+" " + input[1]+" " + input[2]+" " + input[3]+ " " + input[4]+ " " + input[5]);   
       //  console.getOut().println(ll+"a-> "+ inputNN[ll][0]+" "+ inputNN[ll][1]+" "+ inputNN[ll][2]+" "+ inputNN[ll][3]+" "+ inputNN[ll][4]+" "+ inputNN[ll][5]);   
       //  console.getOut().println(ll+"b-> "+ inputNN[ll][95]+" "+ inputNN[ll][94]+" "+ inputNN[ll][93]+" "+ inputNN[ll][92]+" "+ inputNN[ll][91]+" "+ inputNN[ll][90]);       
         }
     
       console.getOut().println( " output of 0,0 0,1   "+ inputNN[0][0] + "; "+ inputNN[0][1]+ "; of 0,95 0, 94  " + inputNN[0][95] + "; "+ inputNN[0][94]);           
       console.getOut().println( " output of lastBar,0 lastBar,1   "+ inputNN[lastBar][0] + "; "+ inputNN[lastBar][1]+ "; of 999,95 999, 94  " + inputNN[lastBar][95] + "; "+ inputNN[lastBar][94]);
       
           
        
       console.getOut().println("Starting write Tick data to a file... " ); 
        
        
        
       FileWriter writer;
       FileWriter writerNNinput;
       
       File file;   
       File fileNNinput;         
       file = new File("C:\\Users\\DrM\\OutputM15compareSasha1.txt");      
       fileNNinput = new File("C:\\Users\\DrM\\NNinputM15compareSasha1.txt");   
      
        try {
           writer = new FileWriter(file);      
        /*
        i = 0; 
        while (i <= last ){
         writer.write("tick["+i+"] -> "+ticks.get(i) ); 
          writer.write(System.getProperty("line.separator"));
          i++;
        }   
       */
        i = 0; 
        while (i <= lastBar ){
         writer.write("bar["+i+"] -> "+bars.get(i) ); 
         writer.write(System.getProperty("line.separator"));
          i++;
        }                           
            writer.flush();            
            writer.close();
         } catch (IOException e) {e.printStackTrace();}
           
       
       
       try {   
           writerNNinput = new FileWriter(fileNNinput);
           //writerNNinput.write(bars.size() +" 96 1 ");//info for all downloaded bars
           writerNNinput.write((  lastBar-(FilterSize-1)-1 - ( VolLength+VolatilityStop) +1 ) +" 96 1 ");// only bars valid for teaching
            writerNNinput.write(System.getProperty("line.separator"));
           
          // for (int ll = 0; ll <= lastBar; ll++){ //all downloaded bars
           for (int ll = VolLength+VolatilityStop; ll <= lastBar-(FilterSize-1)-1; ll++){ //only bars valid for teaching nets    
               for (int kk = 0; kk <=length-1 ; kk++){                 
                   writerNNinput.write(String.format("[%3d,%2d]",ll,kk)+" ");   
                   writerNNinput.write(String.format("%.4f", inputNN[ll][kk] ) + "  ");                                                                                  
                    }
              // writerNNinput.write(System.getProperty("line.separator"));     
              // writerNNinput.write(String.format("%.2f", outputNN[ll] ) + " " + String.format("%.6f", DFaveraged[ll] ) + " " + String.format("%.2f",Stop[ll])+" " + String.format("%.2f", TrailingStop[ll]) +" ");
               writerNNinput.write(String.format("%1d",((int)  outputNN[ll]) ) + " ");
               writerNNinput.write(System.getProperty("line.separator"));              
              }     
       
        
            writerNNinput.flush();            
            writerNNinput.close();
         } catch (IOException e) {e.printStackTrace();}
       
       
       
        console.getOut().println("Writing to a file COMPLETE. " );       
                                                                                           
    }

     protected String getLabel(Instrument instrument) {
      String label = instrument.name();
        label = label.substring(0, 2) + label.substring(3, 5);
      //  label = label + (tagCounter++);
        label = label.toLowerCase();
        return label;
    }

     protected double NeoVolatility(int j, int Step) {
        //int Length=500;
        double sumd=0;
        for (int k=0;k<VolLength;k++)
          {
          //was in NeoVoilatility indicator in NeoTradEx:  sumd += MathSqrt((Close[j+k]-Close[j+k+Step])*(Close[j+k]-Close[j+k+Step]))/Point;   
            sumd += Math.abs( bars.get(j-k).getClose()-bars.get(j-k-Step).getClose() )/Instrument.EURUSD.getPipValue();  
           // sumd += Math.abs( bars.get(j-k).getClose()-bars.get(j-k-Step).getClose() )/0.00001;                               
           }
        return (sumd/VolLength);
    }
 

    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {}

    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {}

    @Override
    public void onMessage(IMessage message) throws JFException {}

    @Override
    public void onAccount(IAccount account) throws JFException {}

    @Override
    public void onStop() throws JFException {  console.getOut().println("I finished!!!!");  }

}
