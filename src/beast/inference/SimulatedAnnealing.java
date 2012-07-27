package beast.inference;

import java.io.PrintStream;

import beast.core.Description;
import beast.core.Input;
import beast.core.Loggable;
import beast.core.Logger;
import beast.core.MCMC;
import beast.core.Operator;
import beast.util.Randomizer;

@Description("Maximum likelihood by simulated annealing")
public class SimulatedAnnealing extends MCMC implements Loggable {
	public Input<Double> startTemp = new Input<Double>("startTemp","starting temperature (default 1.0)", 1.0);
	public Input<Double> endTemp = new Input<Double>("endTemp","end temperature. Together with startTemp this " +
			"determines the temperature trajectory (default 1e-4)", 1e-4);
	
	double m_fDeltaLogTemp;
	
	@Override
	public void initAndValidate() throws Exception {
		super.initAndValidate();
		m_fDeltaLogTemp = Math.log(endTemp.get()) - Math.log(startTemp.get());
	}
	
	
	double fTemp;
	
	@Override
    /** main MCMC loop **/ 
    protected void doLoop() throws Exception {
        String sBestXML = state.toXML(0);
        double fBestLogLikelihood = fOldLogLikelihood;
		double fTemp0 = startTemp.get();
		fTemp = fTemp0;
		
		// find lowest log frequency
		int nLogEvery = nChainLength;
		for (Logger logger :  m_loggers.get()) {
			nLogEvery = Math.min(logger.m_pEvery.get(), nLogEvery);
		}
			
        for (int iSample = -nBurnIn; iSample <= nChainLength; iSample++) {
            state.store(iSample);
            if (m_nStoreEvery > 0 && iSample % m_nStoreEvery == 0 && iSample > 0) {
                state.storeToFile(iSample);
            	operatorSchedule.storeToFile();
            }

            Operator operator = operatorSchedule.selectOperator();
            //System.out.print("\n" + iSample + " " + operator.getName()+ ":");
            double fLogHastingsRatio = operator.proposal();
            
            
            if (fLogHastingsRatio != Double.NEGATIVE_INFINITY) {
            	state.storeCalculationNodes();
                state.checkCalculationNodesDirtiness();

                fNewLogLikelihood = posterior.calculateLogP();

                double logAlpha = fNewLogLikelihood - fOldLogLikelihood;
                //System.out.println(logAlpha + " " + fNewLogLikelihood + " " + fOldLogLikelihood);
                if (logAlpha >= 0 || Randomizer.nextDouble() > Math.exp(-Math.exp(logAlpha) * fTemp)) {
                //if (logAlpha >= 0) {
                    // accept
                    fOldLogLikelihood = fNewLogLikelihood;
                    state.acceptCalculationNodes();

                    if (iSample >= 0) {
                        operator.accept();
                    }
                    //System.out.print(" accept");
                } else {
                    // reject
                    if (iSample >= 0) {
                        operator.reject();
                    }
                    state.restore();
                    state.restoreCalculationNodes();
                    //System.out.print(" reject");
                }
                state.setEverythingDirty(false);
            } else {
                // operation failed
                if (iSample > 0) {
                    operator.reject();
                }
                state.restore();
                //System.out.print(" direct reject");
            }
            
            if (fOldLogLikelihood > fBestLogLikelihood) {
                sBestXML = state.toXML(iSample);
                fBestLogLikelihood = fOldLogLikelihood;
            }
            
            
            
            if (iSample % nLogEvery == 0) {
                String sXML = state.toXML(iSample);
            	state.fromXML(sBestXML);
            	fOldLogLikelihood = robustlyCalcPosterior(posterior); 
            	log(iSample);
            	state.fromXML(sXML);
            	fOldLogLikelihood = robustlyCalcPosterior(posterior); 
            }

            
            
            if (bDebug && iSample % 3 == 0 || iSample % 10000 == 0) { 
            	// check that the posterior is correctly calculated at every third
            	// sample, as long as we are in debug mode
                double fLogLikelihood = robustlyCalcPosterior(posterior); 
                if (Math.abs(fLogLikelihood - fOldLogLikelihood) > 1e-6) {
                	reportLogLikelihoods(posterior, "");
                    throw new Exception("At sample "+ iSample + "\nLikelihood incorrectly calculated: " + fOldLogLikelihood + " != " + fLogLikelihood
                    		+ " Operator: " + operator.getClass().getName());
                }
                if (iSample > NR_OF_DEBUG_SAMPLES * 3) {
                	// switch of debug mode once a sufficient large sample is checked
                    bDebug = false;
                }
            } else {
                operator.optimize(logAlpha);
            }
            callUserFunction(iSample);
            
            fTemp = fTemp0 * Math.exp(iSample * m_fDeltaLogTemp / nChainLength);
        }
    }


	@Override
	public void init(PrintStream out) throws Exception {
		out.append("temperature\t");
	}


	@Override
	public void log(int nSample, PrintStream out) {
		out.append(fTemp + "\t");
	}


	@Override
	public void close(PrintStream out) {
	}    

}
