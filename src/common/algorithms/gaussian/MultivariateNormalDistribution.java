package common.algorithms.gaussian;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import common.exceptions.DeserializationException;

public class MultivariateNormalDistribution
{
    private final static String covLabel = "covariances:";
    private final static String meanLabel = "mean:";
    
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
        this.constant_element = Math.log(Math.pow(2 * Math.PI, mean.length)) + Math.log(covariancesMatrixDeterminant);
    }
    
    public double[] getMean() { return mean; }
    public boolean isOk() { return (this.inversedCovariancesMatrix != null); }

    public double logLikelihood(double[] point)
    {
        if (this.inversedCovariancesMatrix == null) return Double.NEGATIVE_INFINITY;
        RealMatrix x = new Array2DRowRealMatrix(new double[][]{point});
        RealMatrix mean = new Array2DRowRealMatrix(new double[][]{this.mean});
        RealMatrix x_minus_mean = x.subtract(mean);
        
        RealMatrix matrixPart = x_minus_mean.multiply(inversedCovariancesMatrix).multiply(x_minus_mean.transpose());
        assert((matrixPart.getColumnDimension() != 1) || (matrixPart.getRowDimension() != 1));
        
        if (matrixPart.getEntry(0, 0) < 0)
            System.err.println("ERROR: " + matrixPart);
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

    public String serialize()
    {
        return "{" + meanLabel + serializeVector(this.mean) + ","
                + covLabel + serializeMatrix(this.covariances) + "}";
    }
    private String serializeMatrix(double[][] matrix)
    {
        String ret = "[";
        for (double[] val : matrix) {
            ret += serializeVector(val) + ",";
        }
        return ret.substring(0, ret.length() - 1) + "]";
    }
    private String serializeVector(double[] vector)
    {
        String ret = "[";
        for (double val : vector) {
            ret += val + ",";
        }
        return ret.substring(0, ret.length() - 1) + "]";
    }
    public static MultivariateNormalDistribution deserialize(String modelData) throws DeserializationException
    {
        int covIndex = modelData.indexOf(covLabel);
        int meanIndex = modelData.indexOf(meanLabel);
        if ((covIndex < 0) || (meanIndex < 0)) throw new DeserializationException("no mean or covariances");
        
        String meanData = modelData.substring(meanIndex + meanLabel.length(), covIndex - 1);
        String covData = modelData.substring(covIndex + covLabel.length(), modelData.length() - 1);
        return new MultivariateNormalDistribution(deserializeVector(meanData), deserializeMatrix(covData));
    }
    private static double[] deserializeVector(String data)
    {
        String[] valuesData = data.substring(1, data.length() - 1).split(",");
        double[] ret = new double[valuesData.length];
        for (int i = 0; i < valuesData.length; ++i)
            ret[i] = Double.valueOf(valuesData[i]);
        return ret;
    }
    private static double[][] deserializeMatrix(String data)
    {
        String[] rowsData = data.substring(2, data.length() - 2).split("\\],\\[");
        double[][] ret = new double[rowsData.length][];
        for (int i = 0; i < ret.length; ++i)
            ret[i] = deserializeVector("[" + rowsData[i] + "]");
        return ret;
    }
}
