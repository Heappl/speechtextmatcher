package algorithms;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import commonExceptions.ImplementationError;

public class MultivariateNormalDistribution
{
    private final double[] mean;
    private final double[][] covariances;
    private final RealMatrix inversedCovariancesMatrix;
    private final double constant_element;
    
    public MultivariateNormalDistribution(double[] mean, double[][] covariances)
    {
        this.covariances = covariances;
        this.mean = mean;
        RealMatrix covariancesMatrix = new Array2DRowRealMatrix(covariances);
        LUDecomposition decomposition = new LUDecomposition(covariancesMatrix);
        double covariancesMatrixDeterminant = decomposition.getDeterminant();
        if (covariancesMatrixDeterminant != 0) {
            this.inversedCovariancesMatrix = decomposition.getSolver().getInverse();
        } else {
            this.inversedCovariancesMatrix = null;
            System.err.println("singular matrix");
        }
        this.constant_element = Math.log(Math.pow(Math.PI, mean.length) * covariancesMatrixDeterminant);
    }
    
    public double[] getMean() { return mean; }
    public boolean isOk() { return (this.inversedCovariancesMatrix != null); }

    public double logLikelihood(double[] point) throws ImplementationError
    {
        if (this.inversedCovariancesMatrix == null) return Double.NEGATIVE_INFINITY;
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
        String ret = "{gaussian mean: [";
        for (int i = 0; i < mean.length; ++i)
            ret += mean[i] + ", ";
        ret = ret.substring(0, ret.length() - 2) + "] ";
        
        if (!isOk()) {
            ret += "covariances: [";
            for (int i = 0; i < covariances.length; ++i) {
                ret += "[";
                for (int j = 0; j < covariances[i].length; ++j) {
                    ret += covariances[i][j] + ", ";
                }
                ret = ret.substring(0, ret.length() - 2) + "], ";
            }
            ret = ret.substring(0, ret.length() - 2) + "]";
        }
        return ret + "}";
    }
}
