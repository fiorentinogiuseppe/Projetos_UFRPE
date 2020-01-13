package weka.core;

import java.io.PrintStream;

























public class Statistics
  implements RevisionHandler
{
  protected static final double MACHEP = 1.1102230246251565E-16D;
  protected static final double MAXLOG = 709.782712893384D;
  protected static final double MINLOG = -745.1332191019412D;
  protected static final double MAXGAM = 171.6243769563027D;
  protected static final double SQTPI = 2.5066282746310007D;
  protected static final double SQRTH = 0.7071067811865476D;
  protected static final double LOGPI = 1.1447298858494002D;
  protected static final double big = 4.503599627370496E15D;
  protected static final double biginv = 2.220446049250313E-16D;
  protected static final double[] P0 = { -59.96335010141079D, 98.00107541859997D, -56.67628574690703D, 13.931260938727968D, -1.2391658386738125D };
  





  protected static final double[] Q0 = { 1.9544885833814176D, 4.676279128988815D, 86.36024213908905D, -225.46268785411937D, 200.26021238006066D, -82.03722561683334D, 15.90562251262117D, -1.1833162112133D };
  













  protected static final double[] P1 = { 4.0554489230596245D, 31.525109459989388D, 57.16281922464213D, 44.08050738932008D, 14.684956192885803D, 2.1866330685079025D, -0.1402560791713545D, -0.03504246268278482D, -8.574567851546854E-4D };
  









  protected static final double[] Q1 = { 15.779988325646675D, 45.39076351288792D, 41.3172038254672D, 15.04253856929075D, 2.504649462083094D, -0.14218292285478779D, -0.03808064076915783D, -9.332594808954574E-4D };
  













  protected static final double[] P2 = { 3.2377489177694603D, 6.915228890689842D, 3.9388102529247444D, 1.3330346081580755D, 0.20148538954917908D, 0.012371663481782003D, 3.0158155350823543E-4D, 2.6580697468673755E-6D, 6.239745391849833E-9D };
  









  protected static final double[] Q2 = { 6.02427039364742D, 3.6798356385616087D, 1.3770209948908132D, 0.21623699359449663D, 0.013420400608854318D, 3.2801446468212774E-4D, 2.8924786474538068E-6D, 6.790194080099813E-9D };
  








  public Statistics() {}
  








  public static double binomialStandardError(double p, int n)
  {
    if (n == 0) {
      return 0.0D;
    }
    return Math.sqrt(p * (1.0D - p) / n);
  }
  









  public static double chiSquaredProbability(double x, double v)
  {
    if ((x < 0.0D) || (v < 1.0D)) return 0.0D;
    return incompleteGammaComplement(v / 2.0D, x / 2.0D);
  }
  








  public static double FProbability(double F, int df1, int df2)
  {
    return incompleteBeta(df2 / 2.0D, df1 / 2.0D, df2 / (df2 + df1 * F));
  }
  
























  public static double normalProbability(double a)
  {
    double x = a * 0.7071067811865476D;
    double z = Math.abs(x);
    double y;
    double y; if (z < 0.7071067811865476D) { y = 0.5D + 0.5D * errorFunction(x);
    } else {
      y = 0.5D * errorFunctionComplemented(z);
      if (x > 0.0D) y = 1.0D - y;
    }
    return y;
  }
  




















  public static double normalInverse(double y0)
  {
    double s2pi = Math.sqrt(6.283185307179586D);
    
    if (y0 <= 0.0D) throw new IllegalArgumentException();
    if (y0 >= 1.0D) throw new IllegalArgumentException();
    int code = 1;
    double y = y0;
    if (y > 0.8646647167633873D) {
      y = 1.0D - y;
      code = 0;
    }
    
    if (y > 0.1353352832366127D) {
      y -= 0.5D;
      double y2 = y * y;
      double x = y + y * (y2 * polevl(y2, P0, 4) / p1evl(y2, Q0, 8));
      x *= s2pi;
      return x;
    }
    
    double x = Math.sqrt(-2.0D * Math.log(y));
    double x0 = x - Math.log(x) / x;
    
    double z = 1.0D / x;
    double x1; double x1; if (x < 8.0D) {
      x1 = z * polevl(z, P1, 8) / p1evl(z, Q1, 8);
    } else
      x1 = z * polevl(z, P2, 8) / p1evl(z, Q2, 8);
    x = x0 - x1;
    if (code != 0)
      x = -x;
    return x;
  }
  








  public static double lnGamma(double x)
  {
    double[] A = { 8.116141674705085E-4D, -5.950619042843014E-4D, 7.936503404577169E-4D, -0.002777777777300997D, 0.08333333333333319D };
    





    double[] B = { -1378.2515256912086D, -38801.631513463784D, -331612.9927388712D, -1162370.974927623D, -1721737.0082083966D, -853555.6642457654D };
    






    double[] C = { -351.81570143652345D, -17064.210665188115D, -220528.59055385445D, -1139334.4436798252D, -2532523.0717758294D, -2018891.4143353277D };
    








    if (x < -34.0D) {
      double q = -x;
      double w = lnGamma(q);
      double p = Math.floor(q);
      if (p == q) throw new ArithmeticException("lnGamma: Overflow");
      double z = q - p;
      if (z > 0.5D) {
        p += 1.0D;
        z = p - q;
      }
      z = q * Math.sin(3.141592653589793D * z);
      if (z == 0.0D) { throw new ArithmeticException("lnGamma: Overflow");
      }
      z = 1.1447298858494002D - Math.log(z) - w;
      return z;
    }
    
    if (x < 13.0D) {
      double z = 1.0D;
      while (x >= 3.0D) {
        x -= 1.0D;
        z *= x;
      }
      while (x < 2.0D) {
        if (x == 0.0D) { throw new ArithmeticException("lnGamma: Overflow");
        }
        z /= x;
        x += 1.0D;
      }
      if (z < 0.0D) z = -z;
      if (x == 2.0D) return Math.log(z);
      x -= 2.0D;
      double p = x * polevl(x, B, 5) / p1evl(x, C, 6);
      return Math.log(z) + p;
    }
    
    if (x > 2.556348E305D) { throw new ArithmeticException("lnGamma: Overflow");
    }
    double q = (x - 0.5D) * Math.log(x) - x + 0.9189385332046728D;
    
    if (x > 1.0E8D) { return q;
    }
    double p = 1.0D / (x * x);
    if (x >= 1000.0D) {
      q += ((7.936507936507937E-4D * p - 0.002777777777777778D) * p + 0.08333333333333333D) / x;
    }
    else
    {
      q += polevl(p, A, 4) / x; }
    return q;
  }
  
























  public static double errorFunction(double x)
  {
    double[] T = { 9.604973739870516D, 90.02601972038427D, 2232.005345946843D, 7003.325141128051D, 55592.30130103949D };
    





    double[] U = { 33.56171416475031D, 521.3579497801527D, 4594.323829709801D, 22629.000061389095D, 49267.39426086359D };
    







    if (Math.abs(x) > 1.0D) return 1.0D - errorFunctionComplemented(x);
    double z = x * x;
    double y = x * polevl(z, T, 4) / p1evl(z, U, 5);
    return y;
  }
  


























  public static double errorFunctionComplemented(double a)
  {
    double[] P = { 2.461969814735305E-10D, 0.5641895648310689D, 7.463210564422699D, 48.63719709856814D, 196.5208329560771D, 526.4451949954773D, 934.5285271719576D, 1027.5518868951572D, 557.5353353693994D };
    









    double[] Q = { 13.228195115474499D, 86.70721408859897D, 354.9377788878199D, 975.7085017432055D, 1823.9091668790973D, 2246.3376081871097D, 1656.6630919416134D, 557.5353408177277D };
    










    double[] R = { 0.5641895835477551D, 1.275366707599781D, 5.019050422511805D, 6.160210979930536D, 7.4097426995044895D, 2.9788666537210022D };
    






    double[] S = { 2.2605286322011726D, 9.396035249380015D, 12.048953980809666D, 17.08144507475659D, 9.608968090632859D, 3.369076451000815D };
    


    double x;
    

    double x;
    

    if (a < 0.0D) x = -a; else {
      x = a;
    }
    if (x < 1.0D) { return 1.0D - errorFunction(a);
    }
    double z = -a * a;
    
    if (z < -709.782712893384D) {
      if (a < 0.0D) return 2.0D;
      return 0.0D;
    }
    
    z = Math.exp(z);
    double q;
    double p; double q; if (x < 8.0D) {
      double p = polevl(x, P, 8);
      q = p1evl(x, Q, 8);
    } else {
      p = polevl(x, R, 5);
      q = p1evl(x, S, 6);
    }
    
    double y = z * p / q;
    
    if (a < 0.0D) { y = 2.0D - y;
    }
    if (y == 0.0D) {
      if (a < 0.0D) return 2.0D;
      return 0.0D;
    }
    return y;
  }
  

























  public static double p1evl(double x, double[] coef, int N)
  {
    double ans = x + coef[0];
    
    for (int i = 1; i < N; i++) { ans = ans * x + coef[i];
    }
    return ans;
  }
  



















  static double polevl(double x, double[] coef, int N)
  {
    double ans = coef[0];
    
    for (int i = 1; i <= N; i++) { ans = ans * x + coef[i];
    }
    return ans;
  }
  








  public static double incompleteGamma(double a, double x)
  {
    if ((x <= 0.0D) || (a <= 0.0D)) { return 0.0D;
    }
    if ((x > 1.0D) && (x > a)) { return 1.0D - incompleteGammaComplement(a, x);
    }
    
    double ax = a * Math.log(x) - x - lnGamma(a);
    if (ax < -709.782712893384D) { return 0.0D;
    }
    ax = Math.exp(ax);
    

    double r = a;
    double c = 1.0D;
    double ans = 1.0D;
    do
    {
      r += 1.0D;
      c *= x / r;
      ans += c;
    }
    while (c / ans > 1.1102230246251565E-16D);
    
    return ans * ax / a;
  }
  








  public static double incompleteGammaComplement(double a, double x)
  {
    if ((x <= 0.0D) || (a <= 0.0D)) { return 1.0D;
    }
    if ((x < 1.0D) || (x < a)) { return 1.0D - incompleteGamma(a, x);
    }
    double ax = a * Math.log(x) - x - lnGamma(a);
    if (ax < -709.782712893384D) { return 0.0D;
    }
    ax = Math.exp(ax);
    

    double y = 1.0D - a;
    double z = x + y + 1.0D;
    double c = 0.0D;
    double pkm2 = 1.0D;
    double qkm2 = x;
    double pkm1 = x + 1.0D;
    double qkm1 = z * x;
    double ans = pkm1 / qkm1;
    double t;
    do {
      c += 1.0D;
      y += 1.0D;
      z += 2.0D;
      double yc = y * c;
      double pk = pkm1 * z - pkm2 * yc;
      double qk = qkm1 * z - qkm2 * yc;
      if (qk != 0.0D) {
        double r = pk / qk;
        double t = Math.abs((ans - r) / r);
        ans = r;
      } else {
        t = 1.0D;
      }
      pkm2 = pkm1;
      pkm1 = pk;
      qkm2 = qkm1;
      qkm1 = qk;
      if (Math.abs(pk) > 4.503599627370496E15D) {
        pkm2 *= 2.220446049250313E-16D;
        pkm1 *= 2.220446049250313E-16D;
        qkm2 *= 2.220446049250313E-16D;
        qkm1 *= 2.220446049250313E-16D;
      }
    } while (t > 1.1102230246251565E-16D);
    
    return ans * ax;
  }
  



  public static double gamma(double x)
  {
    double[] P = { 1.6011952247675185E-4D, 0.0011913514700658638D, 0.010421379756176158D, 0.04763678004571372D, 0.20744822764843598D, 0.4942148268014971D, 1.0D };
    







    double[] Q = { -2.3158187332412014E-5D, 5.396055804933034E-4D, -0.004456419138517973D, 0.011813978522206043D, 0.035823639860549865D, -0.23459179571824335D, 0.0714304917030273D, 1.0D };
    












    double q = Math.abs(x);
    
    if (q > 33.0D) {
      if (x < 0.0D) {
        double p = Math.floor(q);
        if (p == q) throw new ArithmeticException("gamma: overflow");
        int i = (int)p;
        double z = q - p;
        if (z > 0.5D) {
          p += 1.0D;
          z = q - p;
        }
        z = q * Math.sin(3.141592653589793D * z);
        if (z == 0.0D) throw new ArithmeticException("gamma: overflow");
        z = Math.abs(z);
        z = 3.141592653589793D / (z * stirlingFormula(q));
        
        return -z;
      }
      return stirlingFormula(x);
    }
    

    double z = 1.0D;
    while (x >= 3.0D) {
      x -= 1.0D;
      z *= x;
    }
    
    while (x < 0.0D) {
      if (x == 0.0D) {
        throw new ArithmeticException("gamma: singular");
      }
      if (x > -1.0E-9D) {
        return z / ((1.0D + 0.5772156649015329D * x) * x);
      }
      z /= x;
      x += 1.0D;
    }
    
    while (x < 2.0D) {
      if (x == 0.0D) {
        throw new ArithmeticException("gamma: singular");
      }
      if (x < 1.0E-9D) {
        return z / ((1.0D + 0.5772156649015329D * x) * x);
      }
      z /= x;
      x += 1.0D;
    }
    
    if ((x == 2.0D) || (x == 3.0D)) { return z;
    }
    x -= 2.0D;
    double p = polevl(x, P, 6);
    q = polevl(x, Q, 7);
    return z * p / q;
  }
  




  static double stirlingFormula(double x)
  {
    double[] STIR = { 7.873113957930937E-4D, -2.2954996161337813E-4D, -0.0026813261780578124D, 0.0034722222160545866D, 0.08333333333334822D };
    





    double MAXSTIR = 143.01608D;
    
    double w = 1.0D / x;
    double y = Math.exp(x);
    
    w = 1.0D + w * polevl(w, STIR, 4);
    
    if (x > MAXSTIR)
    {
      double v = Math.pow(x, 0.5D * x - 0.25D);
      y = v * (v / y);
    } else {
      y = Math.pow(x, x - 0.5D) / y;
    }
    y = 2.5066282746310007D * y * w;
    return y;
  }
  










  public static double incompleteBeta(double aa, double bb, double xx)
  {
    if ((aa <= 0.0D) || (bb <= 0.0D)) { throw new ArithmeticException("ibeta: Domain error!");
    }
    
    if ((xx <= 0.0D) || (xx >= 1.0D)) {
      if (xx == 0.0D) return 0.0D;
      if (xx == 1.0D) return 1.0D;
      throw new ArithmeticException("ibeta: Domain error!");
    }
    
    boolean flag = false;
    if ((bb * xx <= 1.0D) && (xx <= 0.95D)) {
      double t = powerSeries(aa, bb, xx);
      return t;
    }
    
    double w = 1.0D - xx;
    double x;
    double a;
    double b; double xc; double x; if (xx > aa / (aa + bb)) {
      flag = true;
      double a = bb;
      double b = aa;
      double xc = xx;
      x = w;
    } else {
      a = aa;
      b = bb;
      xc = w;
      x = xx;
    }
    
    if ((flag) && (b * x <= 1.0D) && (x <= 0.95D)) {
      double t = powerSeries(a, b, x);
      if (t <= 1.1102230246251565E-16D) t = 0.9999999999999999D; else
        t = 1.0D - t;
      return t;
    }
    

    double y = x * (a + b - 2.0D) - (a - 1.0D);
    if (y < 0.0D) {
      w = incompleteBetaFraction1(a, b, x);
    } else {
      w = incompleteBetaFraction2(a, b, x) / xc;
    }
    



    y = a * Math.log(x);
    double t = b * Math.log(xc);
    if ((a + b < 171.6243769563027D) && (Math.abs(y) < 709.782712893384D) && (Math.abs(t) < 709.782712893384D)) {
      t = Math.pow(xc, b);
      t *= Math.pow(x, a);
      t /= a;
      t *= w;
      t *= gamma(a + b) / (gamma(a) * gamma(b));
      if (flag) {
        if (t <= 1.1102230246251565E-16D) t = 0.9999999999999999D; else
          t = 1.0D - t;
      }
      return t;
    }
    
    y += t + lnGamma(a + b) - lnGamma(a) - lnGamma(b);
    y += Math.log(w / a);
    if (y < -745.1332191019412D) {
      t = 0.0D;
    } else {
      t = Math.exp(y);
    }
    if (flag) {
      if (t <= 1.1102230246251565E-16D) t = 0.9999999999999999D; else
        t = 1.0D - t;
    }
    return t;
  }
  








  public static double incompleteBetaFraction1(double a, double b, double x)
  {
    double k1 = a;
    double k2 = a + b;
    double k3 = a;
    double k4 = a + 1.0D;
    double k5 = 1.0D;
    double k6 = b - 1.0D;
    double k7 = k4;
    double k8 = a + 2.0D;
    
    double pkm2 = 0.0D;
    double qkm2 = 1.0D;
    double pkm1 = 1.0D;
    double qkm1 = 1.0D;
    double ans = 1.0D;
    double r = 1.0D;
    int n = 0;
    double thresh = 3.3306690738754696E-16D;
    do {
      double xk = -(x * k1 * k2) / (k3 * k4);
      double pk = pkm1 + pkm2 * xk;
      double qk = qkm1 + qkm2 * xk;
      pkm2 = pkm1;
      pkm1 = pk;
      qkm2 = qkm1;
      qkm1 = qk;
      
      xk = x * k5 * k6 / (k7 * k8);
      pk = pkm1 + pkm2 * xk;
      qk = qkm1 + qkm2 * xk;
      pkm2 = pkm1;
      pkm1 = pk;
      qkm2 = qkm1;
      qkm1 = qk;
      
      if (qk != 0.0D) r = pk / qk;
      double t; if (r != 0.0D) {
        double t = Math.abs((ans - r) / r);
        ans = r;
      } else {
        t = 1.0D;
      }
      if (t < thresh) { return ans;
      }
      k1 += 1.0D;
      k2 += 1.0D;
      k3 += 2.0D;
      k4 += 2.0D;
      k5 += 1.0D;
      k6 -= 1.0D;
      k7 += 2.0D;
      k8 += 2.0D;
      
      if (Math.abs(qk) + Math.abs(pk) > 4.503599627370496E15D) {
        pkm2 *= 2.220446049250313E-16D;
        pkm1 *= 2.220446049250313E-16D;
        qkm2 *= 2.220446049250313E-16D;
        qkm1 *= 2.220446049250313E-16D;
      }
      if ((Math.abs(qk) < 2.220446049250313E-16D) || (Math.abs(pk) < 2.220446049250313E-16D)) {
        pkm2 *= 4.503599627370496E15D;
        pkm1 *= 4.503599627370496E15D;
        qkm2 *= 4.503599627370496E15D;
        qkm1 *= 4.503599627370496E15D;
      }
      n++; } while (n < 300);
    
    return ans;
  }
  








  public static double incompleteBetaFraction2(double a, double b, double x)
  {
    double k1 = a;
    double k2 = b - 1.0D;
    double k3 = a;
    double k4 = a + 1.0D;
    double k5 = 1.0D;
    double k6 = a + b;
    double k7 = a + 1.0D;
    double k8 = a + 2.0D;
    
    double pkm2 = 0.0D;
    double qkm2 = 1.0D;
    double pkm1 = 1.0D;
    double qkm1 = 1.0D;
    double z = x / (1.0D - x);
    double ans = 1.0D;
    double r = 1.0D;
    int n = 0;
    double thresh = 3.3306690738754696E-16D;
    do {
      double xk = -(z * k1 * k2) / (k3 * k4);
      double pk = pkm1 + pkm2 * xk;
      double qk = qkm1 + qkm2 * xk;
      pkm2 = pkm1;
      pkm1 = pk;
      qkm2 = qkm1;
      qkm1 = qk;
      
      xk = z * k5 * k6 / (k7 * k8);
      pk = pkm1 + pkm2 * xk;
      qk = qkm1 + qkm2 * xk;
      pkm2 = pkm1;
      pkm1 = pk;
      qkm2 = qkm1;
      qkm1 = qk;
      
      if (qk != 0.0D) r = pk / qk;
      double t; if (r != 0.0D) {
        double t = Math.abs((ans - r) / r);
        ans = r;
      } else {
        t = 1.0D;
      }
      if (t < thresh) { return ans;
      }
      k1 += 1.0D;
      k2 -= 1.0D;
      k3 += 2.0D;
      k4 += 2.0D;
      k5 += 1.0D;
      k6 += 1.0D;
      k7 += 2.0D;
      k8 += 2.0D;
      
      if (Math.abs(qk) + Math.abs(pk) > 4.503599627370496E15D) {
        pkm2 *= 2.220446049250313E-16D;
        pkm1 *= 2.220446049250313E-16D;
        qkm2 *= 2.220446049250313E-16D;
        qkm1 *= 2.220446049250313E-16D;
      }
      if ((Math.abs(qk) < 2.220446049250313E-16D) || (Math.abs(pk) < 2.220446049250313E-16D)) {
        pkm2 *= 4.503599627370496E15D;
        pkm1 *= 4.503599627370496E15D;
        qkm2 *= 4.503599627370496E15D;
        qkm1 *= 4.503599627370496E15D;
      }
      n++; } while (n < 300);
    
    return ans;
  }
  






  static double powerSeries(double a, double b, double x)
  {
    double ai = 1.0D / a;
    double u = (1.0D - b) * x;
    double v = u / (a + 1.0D);
    double t1 = v;
    double t = u;
    double n = 2.0D;
    double s = 0.0D;
    double z = 1.1102230246251565E-16D * ai;
    while (Math.abs(v) > z) {
      u = (n - b) * x / n;
      t *= u;
      v = t / (a + n);
      s += v;
      n += 1.0D;
    }
    s += t1;
    s += ai;
    
    u = a * Math.log(x);
    if ((a + b < 171.6243769563027D) && (Math.abs(u) < 709.782712893384D)) {
      t = gamma(a + b) / (gamma(a) * gamma(b));
      s = s * t * Math.pow(x, a);
    } else {
      t = lnGamma(a + b) - lnGamma(a) - lnGamma(b) + u + Math.log(s);
      if (t < -745.1332191019412D) s = 0.0D; else
        s = Math.exp(t);
    }
    return s;
  }
  




  public String getRevision()
  {
    return RevisionUtils.extract("$Revision: 5619 $");
  }
  



  public static void main(String[] ops)
  {
    System.out.println("Binomial standard error (0.5, 100): " + binomialStandardError(0.5D, 100));
    
    System.out.println("Chi-squared probability (2.558, 10): " + chiSquaredProbability(2.558D, 10.0D));
    
    System.out.println("Normal probability (0.2): " + normalProbability(0.2D));
    
    System.out.println("F probability (5.1922, 4, 5): " + FProbability(5.1922D, 4, 5));
    
    System.out.println("lnGamma(6): " + lnGamma(6.0D));
  }
}
