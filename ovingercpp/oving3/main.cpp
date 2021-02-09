#include <iostream>
#include <thread>
#include <vector>
#include <list> 
#include <mutex>

using namespace std;

mutex prime_mutex; //mutex to make sure that only one thread is accessed at a time
vector<int> allPrimes; 


//findPrime method that adds all primes in a given interval, to the global prime vector
void findPrime(unsigned int start, unsigned int stop, unsigned int threadNr){
  //cout << "start: " << start << "\nstop: " << stop << " thread: " << threadNr <<endl;  
  for (unsigned int i = start; i <= stop; i++){
    for (unsigned int j = 2; j < i; j++){
      if (i % j == 0) break;
      else if (j + 1 == i) {
        if (allPrimes[allPrimes.size() - 1] != i){ //just making sure we are not adding any duplicates
        unique_lock<mutex> lock(prime_mutex); //locks the prime mutex, destroyed at the end of scope 
        allPrimes.push_back(i); 
        }
      }
    }
  }
}

//multipleThreads that creates a given amount of threads, to work on finding prime numbers in a given interval
void multipleThreads(unsigned int nrOfThreads, unsigned int start, unsigned int end){
    vector<thread> threads;
    int to = (end - start) / nrOfThreads;  
    for (unsigned int i = 0; i < nrOfThreads; i++) {
      //cout << "from " << start << " to " << start + to << " " << endl; 
      threads.emplace_back(findPrime, start, start + to, i);
      start += to; 
    }
    if (start < end){
      threads.emplace_back(findPrime, start, end, nrOfThreads); //if there are unchecked numbers left, the last thread will take care of it 
    }

  for (auto &thread : threads)
    thread.join();
}

//sorting algorithm grom GeeksForGeeks
//https://www.geeksforgeeks.org/bubble-sort/

void swap(int *xp, int *yp) 
{ 
    int temp = *xp; 
    *xp = *yp; 
    *yp = temp; 
} 
  
// An optimized version of Bubble Sort 
void bubbleSort(vector<int> arr, int n) 
{ 
   int i, j; 
   bool swapped; 
   for (i = 0; i < n-1; i++) 
   { 
     swapped = false; 
     for (j = 0; j < n-i-1; j++) 
     { 
        if (arr[j] > arr[j+1]) 
        { 
           swap(&arr[j], &arr[j+1]); 
           swapped = true; 
        } 
     } 
  
     // IF no two elements were swapped by inner loop, then break 
     if (swapped == false) 
        break; 
   }
  //printing out the numbers in order, with a maximum of 10 on each line
  int count = 1; 
   for (int x : arr){
        cout << x << " ";
        if (count % 10 == 0) cout << "\n";
        count += 1; 
  } 
} 

int main() {
  unsigned int start = 0;
  if (start <= 2) allPrimes.push_back(2); //the number two is also a prime number apparently... 
  unsigned int end = 10000;
  int threadsnr = 10; 

  multipleThreads(threadsnr, start, end); 
  bubbleSort(allPrimes, allPrimes.size());
}

