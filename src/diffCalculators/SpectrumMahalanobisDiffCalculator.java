package diffCalculators;
import java.util.ArrayList;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import common.Data;
import common.DataSequence;




public class SpectrumMahalanobisDiffCalculator implements ISpectrumDiffCalculator
{
	RealMatrix inversedCovariancesMatrix = null;
	
	public SpectrumMahalanobisDiffCalculator(DataSequence allData)
	{
		int spectrumSize = allData.get(0).getSpectrum().length;
		double[][] covariances = new double[spectrumSize][spectrumSize];
		
		double[] means = new double[spectrumSize];
		for (Data data : allData) {
			for (int k = 0; k < spectrumSize; ++k)
				means[k] += data.getSpectrum()[k];
		}
		for (int k = 0; k < spectrumSize; ++k) means[k] /= allData.size();
		
		for (int i = 0; i < allData.size(); ++i) {
			double[] aux = new double[spectrumSize];
			for (int k = 0; k < spectrumSize; ++k)
				aux[k] = allData.get(i).getSpectrum()[k] - means[k];
			for (int k = 0; k < spectrumSize; ++k)
				for (int j = 0; j < spectrumSize; ++j)
					covariances[k][j] += aux[k] * aux[j];
		}
		for (int k = 0; k < spectrumSize; ++k)
			for (int j = 0; j < spectrumSize; ++j)
				covariances[k][j] /= allData.size() - 1;
		
		RealMatrix covariancesMatrix = new Array2DRowRealMatrix(covariances);
		this.inversedCovariancesMatrix = new LUDecomposition(covariancesMatrix).getSolver().getInverse();
	}
	
	public double diff(double[] first, double[] second)
	{
		RealMatrix firstMatrix = new Array2DRowRealMatrix(new double[][]{first});
		RealMatrix secondMatrix = new Array2DRowRealMatrix(new double[][]{second});
		RealMatrix vecDiff = firstMatrix.subtract(secondMatrix);
		RealMatrix result = vecDiff.multiply(inversedCovariancesMatrix).multiply(vecDiff.transpose());
		return result.getEntry(0, 0);
	}
}
