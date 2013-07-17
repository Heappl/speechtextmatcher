package algorithms;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import commonExceptions.ImplementationError;

public class MultivariateNormalDistribution
{
    private final double[] mean;
    private final RealMatrix inversedCovariancesMatrix;
    private final double constant_element;
    
    public MultivariateNormalDistribution(double[] mean, double[][] covariances)
    {
        double d = mean.length;
        RealMatrix covariancesMatrix = new Array2DRowRealMatrix(covariances);
        LUDecomposition decomposition = new LUDecomposition(covariancesMatrix);
        double covariancesMatrixDeterminant = decomposition.getDeterminant();
        this.inversedCovariancesMatrix = decomposition.getSolver().getInverse();
        this.constant_element = Math.log(Math.pow(Math.PI, mean.length) * covariancesMatrixDeterminant);
        this.mean = mean;
    }
    
    public double[] getMean() { return mean; }

    public double logLikelihood(double[] point) throws ImplementationError
    {
        RealMatrix x = new Array2DRowRealMatrix(new double[][]{point});
        RealMatrix mean = new Array2DRowRealMatrix(new double[][]{this.mean});
        RealMatrix x_minus_mean = x.subtract(mean);
        
        RealMatrix matrixPart = x_minus_mean.multiply(inversedCovariancesMatrix.multiply(x_minus_mean.transpose()));
        if ((matrixPart.getColumnDimension() != 1) || (matrixPart.getRowDimension() != 1))
            throw new ImplementationError("result is not a single value");
        
        double ret = this.constant_element + matrixPart.getEntry(0, 0);
        return -ret / 2.0;
    }
    
    public String toString()
    {
        String ret = "guassian [";
        for (int i = 0; i < mean.length; ++i)
            ret += mean[i] + ", ";
        return ret.substring(0, ret.length() - 2) + "]";
    }
}
