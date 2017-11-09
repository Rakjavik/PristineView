/**
 * Created by rakjavik on 9/23/2017.
 */
var app = angular.module('pristine',[]);
var ctrl = app.controller('pristineController',function($scope,$http,$interval,$location,$timeout) {
    /**
     * Sets list of clients
     */
    $scope.getHostList = function() {
        $http({
            method: 'GET',
            url: '/pristine',
            params : {
                requestType : 'list'
            }
        }).then(function success(response) {
            if($scope.list != response.data) {
                $scope.list = response.data;
            }
        });
    }
    /**
     * Gets from the PristineServer queue with hostname "server"
     */
    $scope.getFromServerQueue = function () {
        $http({
            method: 'GET',
            url: '/pristine',
            params: {
                requestType: 'getfromqueue',
                hostname: 'server'
            }
        }).then(function success (response) {
            if(response.data.requestType == 'notify') {
                var additionalInfo = response.data.additionalInfo;
                console.log("Notify received - " + additionalInfo);

                if(additionalInfo.includes('received') ) {
                    $scope.responseReceived = true;
                }
                else if (additionalInfo.includes('complete')) {
                    $scope.responseComplete = true;
                    $scope.enableSend = true;
                }
            }
        });
    }
    /**
     * Sends a pristine request to the server queue
     */
    $scope.sendToQueue = function(){
        $scope.responseComplete = false;
        $scope.responseReceived = false;
        $scope.enableSend = false;
        $http({
            method: 'GET',
            url: '/pristine',
            params: {
                requestType: 'putinqueue',
                json: $scope.command
            }
        }).then(function success(response) {
            if(!$scope.responseReceived && !$scope.responseComplete) {
                $timeout(function () {
                    $scope.resetToRed()
                }, 10000);
            }
        });
    }
    $scope.resetToRed = function() {
        //console.log("resetting stop light due to time out");
        $scope.responseComplete = false;
        $scope.responseReceived = false;
        $scope.enableSend = true;
    }
    /**
     * Retrieves current cam shot for each host in list
     */
    $scope.getImage = function getImage() {
        $scope.list.forEach(function(host){
            $http({
                method: 'GET',
                url: '/pristine',
                params: {
                    requestType: 'image',
                    host: host.hostname
                }
            }).then(function successCallback(response) {
                if (response.data.file64){
                    var imageSource = "data:image/png;base64," + response.data.file64;
                    host.imageSource = imageSource;
                    host.recording = response.data.host.recording;
                }
            }, function errorCallback(response) {
            });
        });
    }
    /**
     * Get the index in the list of the given hostname
     * @param hostname
     * @returns {number} index
     */
    $scope.getIndexByHostName = function(hostname) {
        for(var count = 0; count < $scope.list.length; count++) {
            if($scope.list[count].hostname == hostname) {
                return count;
            }
        }
        return -1;
    }

    $scope.go = function ( path,params ) {
        $location.path( path ).search(params);
    };

    /**
     * Opens extra options panel, sets selected host, updates command
     * @param arrayPos
     */
    $scope.getDetails = function(arrayPos) {
        if(arrayPos == $scope.selected) {
            $scope.details = !$scope.details;
        } else {
            $scope.selected = true;
        }
        $scope.selected = arrayPos;
        $scope.command = '{"host":{"hostname":"' + $scope.list[arrayPos].hostname + '"}}';
    }

    /**
     * Send commands
     */
    $scope.sendFlix = function() {
        $scope.command = "{'requestType':'ResumeNetflix','host' :{'hostname':'" + $scope.list[$scope.selected].hostname + "'}}";
    }
    $scope.sendListen = function() {
        $scope.command = "{'requestType':'miclisten','host' :{'hostname':'" + $scope.list[$scope.selected].hostname + "'}}";
    }
    $scope.sendListenOff = function() {
        $scope.command = "{'requestType':'micstop','host' :{'hostname':'" + $scope.list[$scope.selected].hostname +  "'}}";
    }
    $scope.sendText = function() {
        $scope.command = "{'requestType':'talk','host' :{'hostname':'" + $scope.list[$scope.selected].hostname +  "'},'additionalInfo':'" + $scope.ttsString + "'}";
    }
    $scope.sendCamOn = function() {
        $scope.command = "{'requestType':'webcamon','host' :{'hostname':'" + $scope.list[$scope.selected].hostname +  "'}}";
    }
    $scope.sendCamOff = function() {
        $scope.command = "{'requestType':'webcamoff','host' :{'hostname':'" + $scope.list[$scope.selected].hostname +  "'}}";
    }
    $scope.sendVolumeUp = function() {
        $scope.command = "{'requestType':'volumeup','host' :{'hostname':'" + $scope.list[$scope.selected].hostname +  "'}}";
    }
    $scope.sendVolumeDown = function() {
        $scope.command = "{'requestType':'volumedown','host' :{'hostname':'" + $scope.list[$scope.selected].hostname +  "'}}";
    }

    console.log("Initializing");
    $scope.list = []; // List of hosts
    $scope.serverHostName = $location.host();
    $scope.details = false; // Display command options
    $scope.command = ""; // Command to send server
    $scope.ttsString = ""; //Text to speech string
    $scope.selected = 0; //Which host was clicked on last
    $scope.responseComplete = true; // Client has finished processing the request
    $scope.responseReceived = true; // Client has received command and is processing
    $scope.enableSend = true; // If the send button is disabled

    $interval(function(){$scope.getHostList();},2000);
    $interval(function () {$scope.getFromServerQueue();},1000);
    $interval(function(){$scope.getImage();},500);
});