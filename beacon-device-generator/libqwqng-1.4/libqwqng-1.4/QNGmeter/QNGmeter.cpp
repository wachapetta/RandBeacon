#ifdef _WIN32
#include <Windows.h>
#include <conio.h>
#endif

#include <iostream>
#include <iomanip>
#include <stdint.h>
#include <ctime>
#include <time.h>

#include <omp.h>
#include <queue>
#include <deque>
#include <vector>
#include <string>
#include <memory>
#include <cmath>

#include "BiasAndAC.h"
#include "Monkey.h"
#include "Serial.h"
#include "Entropy.h"
#include "Stat.h"
#include "Gamma.h"
#include "KolmogorovSmirnov.h"

// qwqng library
#ifdef _WIN32
	#if defined(_DEBUG)
		#import "../Debug/QWQNG.dll" no_namespace named_guids
	#else
		#import "../Release/QWQNG.dll" no_namespace named_guids
	#endif
#elif __linux
	#include <qwqng.hpp>
	QWQNG* qng;
	char* deviceId;
	char* statusString;
	char* readBuffer;
#elif MACOSX
#endif

#define QNG_GEN1_		1
#define QNG_GEN2_		2
#define QNG_GEN3_		3
#define QNG_GEN4_		4

int deviceType_;
int rxBytes;
int blockIntCount;
bool doExit = false;

using namespace std;

#ifdef __linux
	#include <signal.h>
	// Terminal Signal Handler
	void sigintevent(int)
	{
		doExit = true;
	}
#endif

				
int main()
{					
    // try this to resize terminal
//    resizeterm(42, 80);
  
	#ifdef _WIN32
		// hide cursor
		CONSOLE_CURSOR_INFO info;
		info.dwSize = 100;
		info.bVisible = FALSE;
		SetConsoleCursorInfo(GetStdHandle(STD_OUTPUT_HANDLE), &info);
		// clear screen
		system("cls");
	#elif __linux
		// clear screen
		cout << "\E[?25l\E[H\E[2J";
		// Signal handler to catch CTRL-C in terminal
		signal(SIGINT, (__sighandler_t)&sigintevent);
	#elif MACOSX
    #endif

    try
    {
		
        #ifdef _WIN32
			// initialize qwqng
            CoInitialize(NULL);
			IQNGPtr qng;
        	qng.CreateInstance(CLSID_QNG);
			// get device serial number
        	_bstr_t deviceId = qng->DeviceId;
        #elif __linux
			// initialize qwqng
			qng = new QWQNG();
			// get device serial number
			deviceId = new char [17];
			string dId = qng->DeviceID();
			strcpy( deviceId, dId.c_str() );
			
			// set randbytes array size
			deviceType_ = QNG_GEN1_;
			if (deviceId[3] >= '2')
				deviceType_ = QNG_GEN2_;
			if (deviceId[3] >= '4')
				deviceType_ = QNG_GEN3_;
			if (deviceId[3] >= '7')
				deviceType_ = QNG_GEN4_;

			rxBytes = 8192;
			blockIntCount = 2048;
			if (deviceType_ >= QNG_GEN4_) {
				rxBytes = 65536;
				blockIntCount = 16384;
			}

			// memory for random data and error messages
			readBuffer = new char [rxBytes];//8192
        		statusString = new char [64];
        #elif MACOSX
        #endif
		
	// randtest classes
        CBiasAndAC biasAndAc;
        biasAndAc.ResetAll();
        CMonkey oqso;
        CSerial serial;
        CEntropy entropy;
        CGamma gamma;
        CKolmogorovSmirnov ks;

        double serialP = 0.5;
        double serialZ = 0.0;
        double KSP;
        double KSN;

        time_t startTime;
        time(&startTime);
        string sTime = ctime(&startTime);
        char sStartTime[16];
        sTime.copy(sStartTime, 15, 4);

        time_t timeNow;
        time_t timePrev;
        time(&timePrev);
        timePrev -= 8;

        queue<shared_ptr<vector<uint32_t> > > dataQueue;

        double bitsThroughputCount = 0;
        double prevBitsThroughputCount = 0;
        double bitsTestedCount = 0;
        double bitsTestedRatio = 1;
        double prevBitsTestedCount = 0;

		#ifdef _WIN32
		    LARGE_INTEGER countFreq;
		    QueryPerformanceFrequency(&countFreq);
		    LARGE_INTEGER prevCount;
		    QueryPerformanceCounter(&prevCount);
		    LARGE_INTEGER nowCount;
		#elif __linux
			timeval start, stop;
			double newTimeInterval;
			gettimeofday(&start, NULL);
        #elif MACOSX
        #endif

        double throughput = 0;

        vector<double> metaPs;
        vector<double> meterZs;
        vector<int> meterFlags(36, 0);
        double meterScore = 0;
        bool meterFreeze = false;

        omp_set_nested(true);

        #pragma omp parallel sections num_threads(2)
        {

            #pragma omp section
            {
				// keypress 'x' makes doExit = true in next section
                while (doExit == false)
                {
					// get RandBytes
					#ifdef _WIN32
		                _variant_t randvt = qng->RandBytes[(long)rxBytes];//8192
		                VARIANT randvariant = randvt.GetVARIANT();

		                uint32_t* readBuffer;
		                SafeArrayAccessData(randvariant.parray, (void**)&readBuffer);
					#elif __linux
						int status = qng->RandBytes(readBuffer, rxBytes);
						if (status != S_OK)
						{
							string sstring = qng->StatusString();
							strcpy( statusString, sstring.c_str() );
							throw statusString;
						}
					#elif MACOSX
        			#endif

                    shared_ptr<vector<uint32_t> > newBuffer(new vector<uint32_t>(blockIntCount));//2048
                    memcpy(&newBuffer->at(0), readBuffer, sizeof(uint32_t)*blockIntCount);//2048
                    
					#ifdef _WIN32
                    	SafeArrayUnaccessData(randvariant.parray);
					#endif

                    #pragma omp critical
                    {
                        bitsThroughputCount += blockIntCount*32;//2048
                        
                        if (dataQueue.size() < 128)
                            dataQueue.push(newBuffer);
                    }
                }
            }


            #pragma omp section
            {
                while (doExit == false)
                {            
                    // Test data secions run in parallel
                    // 8192 bytes = 2048 32-bit integers
					shared_ptr<vector<uint32_t> > testBuffer = nullptr;
                    #pragma omp critical
                    {
                        if (dataQueue.size() > 0)
                        {
                            testBuffer = dataQueue.front();
                            dataQueue.pop();

                            bitsTestedCount += blockIntCount*32; //2048
                        }
                    }

                    if (testBuffer != nullptr)
                    {
                        #pragma omp parallel sections num_threads(4)
                        {
                            #pragma omp section
                            { for (int i=0; i<blockIntCount; i++) biasAndAc.InsertWord32(testBuffer->at(i)); } //2048
                            #pragma omp section
                            { for (int i=0; i<blockIntCount; i++) oqso.InsertWord32(testBuffer->at(i)); } //2048
                            #pragma omp section
                            { for (int i=0; i<blockIntCount; i++) serial.InsertWord32(testBuffer->at(i)); } //2048
                            #pragma omp section
                            { for (int i=0; i<blockIntCount; i++) entropy.InsertWord32(testBuffer->at(i)); } //2048
                        }
            
                        // Display results
                        time(&timeNow);
                        if (difftime(timeNow, timePrev) >= 10)
                        {
                            // calc rates
							#ifdef _WIN32
		                        QueryPerformanceCounter(&nowCount);
		                        double newTimeInterval = ((double)nowCount.QuadPart - prevCount.QuadPart) / countFreq.QuadPart;
		                        prevCount = nowCount;
							#elif __linux
								gettimeofday(&stop, NULL);
								newTimeInterval = (stop.tv_sec - start.tv_sec);      			// sec
								newTimeInterval += (stop.tv_usec - start.tv_usec) /1000000.0; 	// us to sec
								start = stop;
							#elif MACOSX
        					#endif
	
                            double newBitInterval;
                            double newRate;
                            #pragma omp critical
                            {
                                newBitInterval = (bitsThroughputCount - prevBitsThroughputCount);
                                prevBitsThroughputCount = bitsThroughputCount;
                            }

                            newRate = newBitInterval / newTimeInterval;
                            if (throughput == 0)
                                throughput = newRate;
                            else
                                throughput = (2*throughput + newRate) / 3;

                            double newBitsTestedRatio = (bitsTestedCount-prevBitsTestedCount) / newBitInterval;
                            prevBitsTestedCount = bitsTestedCount;
                            bitsTestedRatio = (2*bitsTestedRatio + newBitsTestedRatio) / 3;
                            if (bitsTestedRatio > 1)
                                bitsTestedRatio = 1;

                            // meta test and meter
                            metaPs.clear();
                            meterZs.clear();

                            if (bitsTestedCount >= 65536)
                            {
                                // Autocorrelation KS test
                                for (int i=0; i<32; i++)
                                {
                                    metaPs.push_back(biasAndAc.AC.P_Chi2[i]);
                                    meterZs.push_back(biasAndAc.AC.cumulativeACZScore[i]);
                                }
	                            double AcKSP;
                                double AcKSN;
                                ks.KSUP(&AcKSP, &AcKSN, &metaPs[0], metaPs.size());

                                // Combined KS test
                                metaPs.push_back(AcKSP);

                                metaPs.push_back(biasAndAc.Bias.P_Chi2);
                                meterZs.push_back(biasAndAc.Bias.cumulativeBiasZScore);
                            }

                            if (bitsTestedCount >= 4194304)
                            {
                                metaPs.push_back(serial.P_Chi2);
                                serialP = gamma.Gamma(128., serial.cumulativeSerialChi2);
                                serialZ = ks.PtoZ(serialP);
                                meterZs.push_back(serialZ);

                                metaPs.push_back(entropy.P_Chi2);
                                meterZs.push_back(entropy.cumulativeZScore);
                            }

                            if (bitsTestedCount >= 10485775)
                            {
                                metaPs.push_back(oqso.P_Chi2);
                                meterZs.push_back(oqso.cumulativeZScore);
                            }

                            if (bitsTestedCount >= 65536)
                            {
                                // This KS is combined AC KSP plus with other tests
                                ks.KSUP(&KSP, &KSN, &metaPs[32], metaPs.size()-32);

                                meterFreeze = false;
                                for (int i=0; i<meterZs.size(); i++)
                                {
                                    // freeze condition
                                    if (fabs(meterZs[i])>4.264897 || (metaPs[i]<0.00001 || metaPs[i]>0.99999))
                                        meterFlags[i] = -1;
                                    // unfreeze condition
                                    if (meterFlags[i] == -1)
                                    {
                                        if (fabs(meterZs[i])<2.326348 && (metaPs[i]>0.01 && metaPs[i]<0.99))
                                            meterFlags[i] = 0;
                                    }

                                    if (meterFlags[i] == -1)
                                        meterFreeze = true;
                                }

                                // meter calc
                                if (meterFreeze == false)
                                    meterScore = log(bitsTestedCount)/log(2.);
                            }


                            cout << endl;
                            cout << "     QNGmeter Console 1.0         Test Type      z-score   p[z<=x]  p[chi2<=x] " << endl;
                            cout << " +---------------------------+------------------------------------------------+" << endl;
                            cout << " |                           |  1/0 Balance       " << setiosflags(ios::fixed) << setprecision(3) << showpos << biasAndAc.Bias.cumulativeBiasZScore << "    " << setprecision(4) << noshowpos << CStat::ZtoP(biasAndAc.Bias.cumulativeBiasZScore) << "    " << biasAndAc.Bias.P_Chi2 << "  |" << endl;
                            cout << " |   Device Serial           |  Serial Test       " << setiosflags(ios::fixed) << setprecision(3) << showpos << serialZ << "    " << setprecision(4) << noshowpos << serialP << "    " << serial.P_Chi2 << "  |" << endl;
                            cout << " |     " << (char*)deviceId << "              |  OQSO Test         " << setiosflags(ios::fixed) << setprecision(3) << showpos << oqso.cumulativeZScore << "    " << setprecision(4) << noshowpos << CStat::ZtoP(oqso.cumulativeZScore) << "    " << oqso.P_Chi2 << "  |" << endl;
                            cout << " |                           |  Entropy Test      " << setiosflags(ios::fixed) << setprecision(3) << showpos << entropy.cumulativeZScore << "    " << setprecision(4) << noshowpos << CStat::ZtoP(entropy.cumulativeZScore) << "    " << serial.P_Chi2 << "  |" << endl;
                            cout << " |                           |    H: " << setiosflags(ios::fixed) << setprecision(9) << entropy.E << "                              |" << endl;
                
                            cout << " |                           |                                                |" << endl;

                            for (int i=1; i<=32; i++)
                            {
                            switch(i)
                            {
                            case 2:
                                cout << " |   Start Time              |";
                                break;
                            case 3:
                                cout << " |     " << sStartTime << "       |";
                                break;
                            case 5:
                                cout << " |   Total Bits Tested       |";
                                break;
                            case 6:
                                cout << " |     " << scientific <<  setw(9) << setprecision(2) << bitsTestedCount << fixed << "             |";
                                break;
                            case 8:
                                cout << " |   Throughput              |";
                                break;
                            case 9:
                                cout << " |     " << setiosflags(ios::fixed) << setw(4) << setprecision(1) << (double)(throughput/1000000.0) << " Mbps             |";
                                break;
                            case 11:
                                cout << " |   Bits Tested Percent     |";
                                break;
                            case 12:
                                cout << " |     "  << setiosflags(ios::fixed) << setw(5) << setprecision(1) << (100*bitsTestedRatio) << "%                |";
                                break;
                            case 18:
                                cout << " |   Meta KS+ Test           |";
                                break;
                            case 19:
                                cout << " |     "  << setiosflags(ios::fixed) << setw(5) << setprecision(3) << KSP << "                 |";
                                break;
                            case 22:
                                cout << " |   Meta KS- Test           |";
                                break;
                            case 23:
                                cout << " |     "  << setiosflags(ios::fixed) << setw(5) << setprecision(3) << KSN << "                 |";
                                break;
                            case 29:
                                cout << " |   QNGmeter Score          |";
                                break;
                            case 30:
                                cout << " |     "  << setiosflags(ios::fixed) << setw(4) << setprecision(1) << abs(meterScore) << ((meterScore<0)? "-" : (meterFreeze==false)? "+" : " ") << "                 |";
                                break;
                            default:
                                cout << " |                           |";
                            }
                            cout << "  " << setw(2) << i << "st  AutoCorr    " << setiosflags(ios::fixed) << setprecision(3) << showpos << biasAndAc.AC.cumulativeACZScore[i-1] << "    " << setprecision(4) << noshowpos << CStat::ZtoP(biasAndAc.AC.cumulativeACZScore[i-1]) << "    " << biasAndAc.AC.P_Chi2[i-1] << "  |" << endl;
                            }
                            cout << " +---------------------------+------------------------------------------------+" << endl;

							#ifdef _WIN32
								// put cursor in top corner
		                        COORD coord;
		                        coord.X = 0;
		                        coord.Y = 0;
		                        SetConsoleCursorPosition(GetStdHandle(STD_OUTPUT_HANDLE), coord);
							#elif __linux
								// clear screen
								printf("\E[H");
							#elif MACOSX
        					#endif

                            timePrev = timeNow;
                        }
            
                        // End on an 'x' keypress
						#ifdef _WIN32
		                    if (kbhit())
		                    {
		                        char c = getch_();
	 
		                        if ( tolower(c) == 'x' )
		                        {
		                            doExit = true;
		                            break;
		                        }
		                    }
						#elif __linux
	                        // CRTL-C 
						#elif MACOSX
        				#endif
                    }
                    else
                    {
                        // give this thread a break from tight loop - waiting for data
                        usleep(1000);
                    }
                }
            }
        }
    }

	#ifdef _WIN32
    	catch(_com_error& e)
		{
		    // Get exception description as a char*
		    _bstr_t bDsc = e.Description();
		    char* sDsc = (char*)bDsc;

		    cout << "Error " << hex << e.Error() << ": " << sDsc << endl;
		}
	#elif __linux
	catch(char* e)
		{
		    // Get exception description as a char*
			cout << endl << "Error: " << e << endl;
		}
	#elif MACOSX
    #endif
    catch(...)
    {
        cout << endl << "Unknown Error" << endl;
    }
	
	#ifdef __linux
		cout << "\E[?25h\E[43B"; // restore curser on terminal
		delete [] deviceId;
		delete [] statusString;
		delete [] readBuffer;
		delete qng;
	#elif MACOSX
    #endif

	return 0;
}
