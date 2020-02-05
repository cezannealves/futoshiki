/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Scanner;

/**
 *
 * @author Cézanne Alves
 */
public class Backtracking {

    /**
     * @param args the command line arguments
     */
    //entrada
    static Scanner in;
    static PrintStream out, csv;
    static int n, d, r;
    static int[][] mInt;
    static int[][] r_list;
    static long treshold = 1000000;
    
    static long begin,start,end, elapsed;
    
    //instancia/execução
    static Cell t[][];
    static Cell[] list;
    static BitSet[] col;
    static BitSet[] row;
    static Cell[] rMin;
    
    static boolean fwFlag = false;
    static boolean mrFlag = false;
    static boolean gaveUp = false;
    static int next;
    static int first;
    static long counter;
    static int ni;

    public static void main(String[] args) throws Exception {
        
        
        
                
//        treshold=1000000000; fwFlag=false; mrFlag= true;

//        in = new Scanner(System.in);
//        in = new Scanner(new File("futoshiki_all.txt"));
//        in = new Scanner(new File("mrv_only.txt"));
//        in = new Scanner(new File("arc_consistent.txt"));
//        in = new Scanner(new File("arc_inconsistent.txt"));
//        in = new Scanner(new File("unsolvable.txt"));
//        in = new Scanner(new File("one_case.txt"));
        
//        out = new PrintStream(new File("out.txt"));
        

        in = new Scanner(System.in);
        out = System.out;
        
        if (args.length > 0){
            if (args[0].equals("fwc")){
                fwFlag=true;
//                out.println("Foward Checking");
            }
            else if (args[0].equals("mrv")){
                mrFlag=true;
//                out.println("MRV");
            }
        }
        
        compute();

//        treshold=1000000; fwFlag=false; mrFlag= true;
//        for (File file : (new File("benchmarks/inputs")).listFiles()) {
//            csv = new PrintStream(new File("benchmarks/time/mrv-t10-"+file.getName()+".csv"));
////            csv = new PrintStream(new File("benchmarks/trash/mrv2.1-t6-"+file.getName()+".csv"));
//            in = new Scanner(file);
//            benchmark();
//        }
        
        System.exit(0);
        
        
//        treshold=1000000000; fwFlag=false; mrFlag= true;
//        for (File file : (new File("benchmarks/inputs")).listFiles()) {
//            out = new PrintStream(new File("benchmarks/output/mrv/out_"+file.getName()));
//            in = new Scanner(file);
//            compute();
//            
//        }
        

//        System.exit(0);
        
        
        
        ////////////////
        //      DEBUG
        //////////////
        int n = in.nextInt();
//        out.println(""+b.size());
//        out.println(""+b.nextClearBit(n));

        for (ni = 0; ni < n; ni++) {
            boolean achou = false;
            loadInputInstance();
            
            inicialize();
            
            
            counter=0;
            achou = backtrack();
//            printState();
//            printInput(ni);
            if(achou){
                //out.println("Achou!");
                if(bruteCheck()) out.println("Check OK");
                else{out.println("Deu erro"); System.in.read();}
//                if(list.length-first==counter); //out.println("MRV OK");
//                else{out.println("Mais atribuiçoes que qtd de celulas"); System.in.read();}
            }
            else out.println(gaveUp ? "Limite excedido" : "não achou :(");
            
//            System.in.read();
            
            
            
        }
        
        
        
    }
    
    static void compute() throws Exception{
        int n = in.nextInt();
        
        for (ni = 0; ni < n; ni++) {
            loadInputInstance();
            inicialize();
            
            boolean achou = false;
            achou = backtrack();
            out.println(ni+1+"");
            if(achou){
                outputInstance();
                if(!bruteCheck()){
                    System.out.println("Deu erro"); System.in.read();
                }
            }
            else out.println(gaveUp ? "Numero de atribuicoes excede limite maximo" : "Solucao nao encontrada");
            
            
        }
        
    }
    
    static void inicialize(){
        t= new Cell[d+2][d+2];
        list = new Cell[d*d];
        col = new BitSet[d+1];
        row = new BitSet[d+1];
        rMin = new Cell[d+1];
        counter=0;
        gaveUp=false;
        next = first = 0;
        
        
        ///preenche a tabela com padding
        ///fiz assim pro caso de precisar olhar pra vizinhança num loop
        for (int i = 0; i < d+2; i++) {
            for (int j = 0; j < d+2; j++) {
                t[i][j] = new Cell(i, j);
            }
        }
        
        
        //colunas e linhas inicialmente vazias
        for (int i = 1; i < d+1; i++) { col[i]= new BitSet();  col[i].set(1, d+1);}
        for (int i = 1; i < d+1; i++) { row[i]= new BitSet();  row[i].set(1, d+1);}
        
        
        ///preenche os valores do tabuleiro
        ///inicia bitsets cheios
        ///poe as fixas, em ordem, no inicio da lista e as marca
        ///e restringe as colunas e linhas delas
        first=0;
        for (int i = 1; i < d+1; i++) {
            for (int j = 1; j < d+1; j++) {
                Cell c=t[i][j];
                c.v = mInt[i-1][j-1];
                c.bStart.set(1,d+1);
                if(c.v!=0){
                    list[first++] = t[i][j];
                    c.fix=true;
                    row[i].set(c.v, false);
                    col[j].set(c.v, false);
                }
            }
        }
        
        
        //Põe as celulas restantes na lista, em ordem, após as fixas
        next = first;
        for (int i = 1; i < d+1; i++) {
            for (int j = 1; j < d+1; j++) {
                if(t[i][j].v==0) list[next++]= t[i][j];
            }
        }
        next=first;
        
        
        
        
        ///monta o grafo de restrições
        /// (restições com celulas fixas vão direto pro domínio)
        for (int[] r : r_list) {
            Cell l = t[r[0]][r[1]], g = t[r[2]][r[3]];
            
            if(g.fix){
                l.bStart.set(g.v, d+1, false);
            }else if(l.fix){
                g.bStart.set(1, l.v+1, false);
            }else{
                l.lthan.add(g);
                g.gthan.add(l);
            }
        }
        
        for (int i = 1; i < d+1; i++) {
            Cell minC= null;
            for (int j = 1; j < d+1; j++) {
                if(t[i][j].v==0  && ( minC==null || t[i][j].nAv()<minC.nAv() ))
                    minC=t[i][j];
            }
            rMin[i]=minC;
        }
        
        
        /////////// Aqui dá pra inicializar algumas bitsets fixos de algumas
        /////////// celulas
        /////////// já da pra fixar a restrição dos que já tem
        /////////// vizinhos no tabuleiro no início
        /////////// 
        ///////////  ##########FWd checking##########
        /////////// alem de já cortar qtd de maiores da parte de cima
        /////////// e a qtd de menores de baixo isso já seria FW checkin

        
        
    }
    
    static boolean backtrack() throws Exception {

        if(next==list.length) return true; ///chegou no fim!!!!
//        if (counter !=0 && counter%1000000 == 0) { printState(); }
//        if(!partialCheck()) {out.println("Infringiu!!!!"); System.in.read(); }

        Cell c = mrFlag? mrv() : list[next]; int i = c.i, j = c.j;
        //           mrv: O(n)  :   list[next]: O(1)
        
        
        
        BitSet av = c.avaliable(); //pega os disponiveis - O(1)
        
        next++; //next é contador e indice de lista
        for (int val = av.nextSetBit(1); val >= 0; val = av.nextSetBit(val+1)){ //O(1) amortizado em cada recursão
            if(counter==treshold){gaveUp=true; break; }//caso tiver estourado o limite
//            if(counter%1000==0 && getUserTime()-start >5000000000L){gaveUp=true; break; }//Quebra por tempo
            c.v=val;  counter++;
            
            col[j].set(val, false); row[i].set(val, false); //O(1)
//            if(mrFlag)mrUpdate(i, j);//O(d) - manutençao do mvr2 tá com bug
                                    //fwCheck - O(d)
            if(mrFlag || !fwFlag || fwCheck2(i,j)){ //pra evitar fwchk com mvr ligado
                if(backtrack() == true) return true;
            }
            
            col[j].set(val); row[i].set(val);  //O(1)
//            if(mrFlag)mrUpdate(i, j);//O(d)
        }
        c.v=0; next--; 
//        if(mrFlag)mrUpdate(i, j);//O(d)
        return false;
    }
    
    static Cell mrv(){
        //[set,set2,set3,[   ], 0, 0, 0, 0, 0....]
        //  places min here^             ^min
        Cell c = list[next]; int min = next;
        for (int i = next+1; i < list.length; i++)
            if(list[i].nAv()<c.nAv()){
                c=list[i];
                min=i;
            }
        list[min]=list[next];    list[next]= c;
        
        
        if(c.v!=0) System.out.println("MRV Tá bugado");
        
//        out.println("MRV!!!");
        
        return c;
    }
    
    static Cell mrv2() throws Exception{
        
        Cell cMin=null;
        for(int i=1; i<d+1; i++){
            if(cMin==null||(rMin[i]!=null&&rMin[i].nAv()< cMin.nAv()))
                cMin =rMin[i];
        }
        
        /////////////////atualiza lista do mrv1 pra comprarar
        
//        int ic;
//        for(ic=next; list[ic]!=cMin; ic++);
//        
//        list[ic]=list[next];list[next]=cMin;
        
        
        for (int i = next + 1; i < list.length; i++) {
            if (list[i] == cMin) {
                list[i] = list[next]; list[next] = cMin; break;
            }
        }
        
        Cell ref = mrv();
        
        if(ref.nAv()<cMin.nAv()){
            System.out.println("mrv2 retornou subotimo"); //System.in.read();
        }

        ////////////////
        
        
        int ic =cMin.i;//nunca termina nulo pq sempre tem alguma célula válida
//        ******tá errado, vou atualizar com o cmin de novo. e nem precisa,\
//        ***sempre tem mrUpdate
        Cell cR = null;
        for(int j=1; j<d+1; j++){
            if(t[ic][j].v==0  && t[ic][j]!=cMin && ( cR==null || t[ic][j].nAv()<cR.nAv() ))
                cR=t[ic][j];
        }
        rMin[ic]=cR;
//        
        
        return cMin;
        
    }
    
    static Cell mrv3() throws Exception{
        
        Cell cMin=null;// busca o menor dos menores
        int min = d+1, av =d+2;
        for(int i=1; i<d+1; i++){
            if(rMin[i]!=null) av =rMin[i].nAv();
            if(av<min){
                min =av;
                cMin=rMin[i];
            }
        }
        
        
//        int ic =cMin.i;//nunca termina nulo pq sempre tem alguma célula válida
//        
//        ***** talvez eu nem precise atualizar aqui 
//                q depois de todo fetch vem um update;
//        Cell cR = null;
//        min=d+1; av=d+2;
//        for(int j=1; j<d+1; j++){
//            if(t[ic][j].v==0&&t[ic][j]!=cMin) av = t[ic][j].nAv();
//            if(av <min){ min=av; cR=t[ic][j];}
//        }
//        rMin[ic]=cR; //pode ser null

        /////////////// Atualiza pra testar com o mrv1
        
        
        
        for (int i = next + 1; i < list.length; i++) {
            if (list[i] == cMin) {
                list[i] = list[next]; list[next] = cMin; break;
            }
        }
        
        Cell ref = mrv();
        
        if(ref.nAv()<cMin.nAv()){
            System.out.println("mrv3 retornou subotimo"); //System.in.read();
        }
        
        

        
        
        
//        try {
//            int ic;
//            for (ic = next; list[ic] != cMin; ic++);
//            
//            list[ic] = list[next];
//            list[next] = cMin;
//        } catch (Exception e) {
//            System.out.println("error");
//        }
        ////////////////////
        
        return cMin;
    }
    
    static void mrUpdate(int ic, int jc){
        
        Cell minC = null;
        for(int j=1; j<d+1; j++){
            if(t[ic][j].v==0  && ( minC==null || t[ic][j].nAv()<minC.nAv() ))
                minC=t[ic][j];
        }
        if(rMin[ic]==null|| rMin[ic].v!=0 ||(minC!=null&& minC.nAv()<rMin[ic].nAv()))
            rMin[ic]=minC;
        
        for(int i=1; i<d+1; i++){
            if(t[i][jc].v==0 && (rMin[i]==null||rMin[i].v!=0 || t[i][jc].nAv()<rMin[i].nAv()))
                rMin[i]=t[i][jc];
                
            if(rMin[i]!=null && rMin[i].v!=0)
                rMin[i]=null;
        }
        
    }
    
    static void mrUpdate2(int ic, int jc){
        //checa coluna. há poucas possibilidades quando i!=ic
        // o que for feito quando i==ic será sobrecrito embaixo
        for(int i=1; i<d+1; i++){
            if(t[i][jc].v==0 && rMin[i]!=null)
                if(rMin[i].nAv()<t[i][jc].nAv())
                    rMin[i]=t[i][jc];
        }
        
        
//        //quando i == ic varre a linha toda em busa do novo menor
//        Cell minC = null;
//        int min=d+1, av =d+2;//av<min apenas se houver cell vazia
//        for(int j=1; j<d+1; j++){
//            if(t[ic][j].v==0) av=t[ic][j].nAv();
//            if(av<min ){ min = av; minC=t[ic][j]; }
//        }
//        rMin[ic]=minC; //mrv da linha ic ou null se não hover vazias

        Cell minC= null;
        for (int j = 1; j < d+1; j++) {
            if(t[ic][j].v==0  && ( minC==null || t[ic][j].nAv()<minC.nAv() ))
                minC=t[ic][j];
        }
        rMin[ic]=minC;
        
        
        
    }
    
    static boolean fwCheck(){
        for (int i = next; i < list.length; i++){
            if(list[i].nAv()==0) return false;
        }
        return true;
    }
    
    static boolean fwCheck2(int ic,int jc){
        //[ic],[jc] acaba de ser atribuido
        for (int i = 1; i < d+1; i++) {
            if(t[i][jc].v==0 && t[i][jc].nAv()==0) return false;
        }
        for (int j = 1; j < d+1; j++) {
            if(t[ic][j].v==0 && t[ic][j].nAv()==0) return false;
        }
        
//        out.println("FW checking!!!");
        
        return true;
    }
    
    static void benchmark() throws Exception{
    

//        in = new Scanner(new File("futoshiki_all.txt"));
//        in = new Scanner(new File("mrv_only.txt"));
//        in = new Scanner(new File("arc_consistent.txt"));
//        in = new Scanner(new File("arc_inconsistent.txt"));
//        in = new Scanner(new File("one_case.txt"));
        
//        fwFlag=true; mrFlag=false; treshold=1000000;
//        csv = new PrintStream(new File("benchmarks/ver2/db1_fwc_t^6.csv"));
        
        
        
        int n = in.nextInt();
        
        csv.println("case; size; FwCheck; MRV; gaveUp; found; attributions; seconds");

        for (ni = 0; ni < n; ni++) {
            loadInputInstance();
            
            
            start = getUserTime();
            inicialize();
            
            boolean achou = false;
            achou = backtrack();
            elapsed=getUserTime()-start;
            
            if(achou){
                out.println("Achou!");
                if(bruteCheck())out.println("Check OK");
                else{out.println("Deu erro"); System.in.read();}
            }
            else out.println(gaveUp ? "Limite excedido" : "não achou :(");
            
            String s= ni+1+"; "+d+"; "+fwFlag+"; "+mrFlag+"; "+gaveUp+"; "+achou
                    +"; "+counter+"; "+elapsed/1000000000.0;
            csv.println(s);
            
        }
        
        
    }
    
    static boolean bruteCheck(){
        
        HashSet<Integer> set = new HashSet<>();
        for (int i = 0; i < d; i++) {
            set.clear();
            for (int j = 0; j < d; j++) {
                if(mInt[i][j]!=0 && t[i+1][j+1].v != mInt[i][j]) return false;
                if(t[i+1][j+1].v>d || t[i+1][j+1].v<1) return false;
                if (!set.add(t[i+1][j+1].v)) return false; 
            }
        }
        for (int j = 0; j < d; j++) {
            set.clear();
            for (int i = 0; i < d; i++) {
                if (!set.add(t[i+1][j+1].v)) return false; 
            }
        }
        
        for (int[] r : r_list) {
            if(t[r[0]][r[1]].v >= t[r[2]][r[3]].v) return false;
        }
        ///  *  persistencia das celulas fixar
        ///  *  todas preenchidas
        ///  *  valores dentro dos limites 1-d
        ///  *  unico na linha
        ///  *  unico na coluna
        ///  *  respeito das restriçoes de vizinhos
        
        return true;
    }
    
    static boolean partialCheck(){
        
        HashSet<Integer> set = new HashSet<>();
        int v;
        for (int i = 0; i < d; i++) {
            set.clear();
            for (int j = 0; j < d; j++) {
                v = t[i + 1][j + 1].v;
                if (mInt[i][j] != 0 && v != mInt[i][j]) {
                    return false;
                }
                if (v > d || v < 0) {
                    return false;
                }
                if (v != 0 && !set.add(v)) {
                    return false;
                }
            }
        }
        for (int j = 0; j < d; j++) {
            set.clear();
            for (int i = 0; i < d; i++) {
                v = t[i + 1][j + 1].v;
                if (v != 0 && !set.add(v)) {
                    return false;
                }
            }
        }

        for (int[] r : r_list) {
            if (t[r[0]][r[1]].v !=0   &&   t[r[2]][r[3]].v !=0
                    &&   t[r[0]][r[1]].v >= t[r[2]][r[3]].v) {
                return false;
            }
        }
        ///  *  persistencia das celulas fixas
        ///    todas preenchidas
        ///  *  valores dentro dos limites 0 a d
        ///  *  unico na linha
        ///  *  unico na coluna
        ///  *  respeito das restriçoes de vizinhos
        
        return true;
    }
    
    static void printState(){
        out.println(+ni+"\n"+counter+" Atribuições");
        for (Cell[] row : Arrays.copyOfRange(t, 1, d+1)) {
            out.println(Arrays.toString(Arrays.copyOfRange(row, 1, d+1)));
        }
//        out.println("");
    }

    static void loadInputInstance() {
        d = in.nextInt();
        r = in.nextInt();

        mInt = new int[d][d];

        r_list = new int[r][4];

        for (int i = 0; i < r; i++) {
            for (int j = 0; j < 4; j++) {
                r_list[i][j] = in.nextInt();
            }
        }

        for (int i = 0; i < r; i++) {

        }

        for (int i = 0; i < d; i++) {
            for (int j = 0; j < d; j++) {
                mInt[i][j] = in.nextInt();
            }
        }
    }
    
    static void outputInstance(){
        
        String s;
        for (int i = 1; i < d+1; i++) {
            s="";
            for (int j = 1; j < d+1; j++) {
                s+=t[i][j]+" ";
            }
            out.println(s.trim());
        }
    }

    static void printInput(int ni) {
        //static void printInput(int ni, int[][] m, int[][] r_l)
        out.println("" + ni);
        out.println(list.length-first+" initially unassigned");

        out.println("Tab");

        for (int i = 0; i < mInt.length; i++) {
            String line = "";
            for (int j = 0; j < mInt.length; j++) {
                line += mInt[i][j]+" ";
            }
            out.println(line.trim());
        }

        out.println("Restrictions");

        for (int i = 0; i < r_list.length; i++) {
            String line = "";
            for (int j = 0; j < 4; j++) {
                line += r_list[i][j]+" ";
            }
            out.println(line.trim());
        }

    }
    
    static long getUserTime( ) {
    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
    return bean.isCurrentThreadCpuTimeSupported( ) ?
        bean.getCurrentThreadUserTime( ) : 0L;
    }

    public static class Cell {

        public int v=0;
        public boolean fix = false;
        int i, j;
        BitSet bStart= new BitSet();
        
        public ArrayList<Cell> lthan = new ArrayList<>();
        public ArrayList<Cell> gthan = new ArrayList<>();
        
        
        public Cell(int i,int j){
            this.i=i; this.j=j; this.v =v;
        }

        public BitSet avaliable() {

            BitSet b = (BitSet) bStart.clone(); ///faço isso pq as restrições\
            // com as celulas fixas não mudam, logo não recomputo elas.
            // de qualquer forma eu teria que instanciar um novo

            //b.set(1,d+1); // inicializa livre
            
            /////////// Aqui dá pra cortar alguns já
            /////////// eu já posso cortar qtd de maiores da parte de cima
            /////////// e a qtd de menores de baixo
            /////////// alem de já fixar a restrição dos que já estão
            /////////// no tabuleiro no início

            for (Cell c : lthan) { if (c.v!=0) b.set(c.v, d+1, false); }
            for (Cell c : gthan) { if (c.v!=0) b.set(1, c.v + 1, false); }
            
            b.and(row[i]); b.and(col[j]);
            return b;
        }
        
        public int nAv(){
            return avaliable().cardinality();
        }
        
        @Override
        public String toString(){
            return(""+v);
        }

    }

}
