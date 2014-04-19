/* 
 * File:   main.cpp
 * Author: towelenee
 *
 * Created on 19 Апрель 2014 г., 20:32
 */

#include <cstdlib>
#include <vector>
#include <set>
#include <algorithm>

using namespace std;


int main(int argc, char** argv) {
    if (argc != 4) {
        return -1;
    }
    int length = atoi(argv[2]);
    int numberOfFiles = 2;
    bool optimized = true;
    std::vector< std::set< std::vector<int> > > sets(numberOfFiles);
    for (int fileId = 0; fileId < numberOfFiles; fileId++)
    {
        std::vector<int> data;
        //Reading
        for (int i = 0; i < data.size() - length; i++) {
            const std::vector<int> vec = std::vector<int>(data.begin() + i, data.begin() + i + length);
            if (!optimized)
                sets[fileId].insert(vec);
            else
            {
                int min = *std::min_element(vec.begin(), vec.end());
                int max = *std::max_element(vec.begin(), vec.end());
                if (min == vec[0] || max == vec[0] || min == *vec.rbegin() || max == *vec.rbegin())
                    sets[fileId].insert(vec);
            }
	}
    }
    for (int i = 0; i < numberOfFiles; i++)
    {
        for (int j = 0; j < numberOfFiles; j++)
        {
            int intersect = 0;
            for (auto it = sets[i].begin(); it != sets[i].end(); ++it)
                if (sets[j].count(*it))
                    intersect++;
            double ans = intersect / (sets[i].size() + sets[j].size() - intersect);
        }
    }
        
    return 0;
}

