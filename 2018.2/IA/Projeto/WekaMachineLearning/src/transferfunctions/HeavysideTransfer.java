public class HeavysideTransfer implements TransferFunction 
{
	@Override
	public double evalute(double value) 
	{
		if(value >= 0.0)
			return 1.0;
		else
			return 0.0;
	}

	@Override
	public double evaluteDerivate(double value) 
	{
		return 1.0;
	}

}
