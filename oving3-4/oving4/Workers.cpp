#include <thread>
#include <mutex>
#include <condition_variable> 
#include <functional>
#include <iostream>
#include <list>
#include <vector> 
#include <iostream> 


using namespace std;

class Workers {
    private:
        bool wait = true; 
        bool stopped = false; 
        mutex tasks_mutex;
        mutex wait_mutex; 
        mutex stop_mutex; 
        condition_variable cv; 
        list<function<void()>> tasks_list; 
        vector<thread> worker_threads; 
        int nr_of_threads; 
    
    public: 
        Workers(int nr){
            nr_of_threads = nr; 
        }

        void start(){
            worker_threads.clear(); 
            for (int i = 0; i < nr_of_threads; i++){
                worker_threads.emplace_back([this, i] {
                    bool finished = false; 
                    bool queue = false; 
                    while (!finished) {
                        function<void()> task; //the task we are doing 
                        if (!queue){
                            unique_lock<mutex> lock(wait_mutex); 
                            //use lock with the wait mutex
                            //as long as out task is not finished (wait has not been set to false by put()
                            //then we have to tell our cv to wait with out waiting lock 
                            while (wait) {
                                cv.wait(lock); 
                            }
                        }
                        {
                            unique_lock<mutex> lock(tasks_mutex); 
                            //if the tasklist is not  empty, we have to get to begin()
                            if (!tasks_list.empty()){
                                task = *tasks_list.begin();
                                tasks_list.pop_front(); 
                                //there are now tasks in the queue
                                queue = true; 
                            } else {
                                {
                                    unique_lock<mutex> lock(stop_mutex);
                                    //if the stop() method has been called, break the loop
                                    if(stopped) {
                                        finished = true;
                                    } else {
                                        //if it hasnt been stopped, we continue waiting a little longer
                                        //until wait is set to false again, and we can exit
                                        unique_lock<mutex> lock(wait_mutex);
                                        wait = true;
                                        queue = false;
                                    }
                                }
                            }
                        }
                        //we need to do the task outside of the mutex lock
                        if (task) {
                            task(); 
                        }
                    }
                    
                });
            }
        }

        //takes in a function as parameter
        void post(function<void()> task){
            {
                unique_lock<mutex> lock(tasks_mutex); //a unique lock for the task with task mutex
                tasks_list.emplace_back(task); 
            }
            {
                unique_lock<mutex> lock(wait_mutex); //creates unique lock with wait mutex
                wait = false;
            }
            cv.notify_all(); //notify_all "awakes" waiting codition variable
        }

        void stop() {
            {
                unique_lock<mutex> lock(stop_mutex); //unique lock with stop mutex
                stopped = true;
            }
            {
                //when stopped, no longer waiting
                unique_lock<mutex> lock(wait_mutex); 
                wait = false; 
            }
            cv.notify_all(); 
        }

        void join() {
            //stops before then joining threads
            {
                stop();
            }
            {
                for (auto &thread : worker_threads){
                    thread.join();
                }
                cout << "Threads joined" << endl; 
            }
        }

        void post_timeout(function<void()> message, int time){
            //a timeout that last "time" milliseconds
            this_thread::sleep_for(std::chrono::milliseconds(time));
            post(message);
        }
};

int main(){
    Workers workers_threads(4); //4 seperate threads, they may run in parallel 
    Workers event_loop(1); //1 thread, event loop work will be sequential (but may be parallel to workers threads)
    
    workers_threads.start(); 
    event_loop.start(); 

    workers_threads.post([] {
        cout << "----- THIS IS THE TASK A -----" << endl; 
    });
    workers_threads.post([] {
        cout << "----- THIS IS THE TASK B -----" << endl; 
    });
    event_loop.post([] {
        cout << "----- THIS IS THE TASK C -----" << endl; 
    });
    event_loop.post([] {
        cout << "----- THIS IS THE TASK D -----" << endl; 
    });

    workers_threads.post_timeout([] {
        cout<<"timeout"<<endl;
        }, 2000);
    workers_threads.post_timeout([] {
        cout<<"timeout"<<endl;
        }, 2000); 

    workers_threads.join(); 
    event_loop.join(); 
}